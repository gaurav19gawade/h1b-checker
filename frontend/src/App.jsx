import { useState, useRef } from 'react'
import styles from './App.module.css'

const STATUS = { IDLE: 'idle', LOADING: 'loading', SUCCESS: 'success', ERROR: 'error' }

function Badge({ sponsors }) {
  if (sponsors === true)
    return <span className={`${styles.badge} ${styles.badgeSuccess}`}>✓ Sponsors H-1B</span>
  if (sponsors === false)
    return <span className={`${styles.badge} ${styles.badgeDanger}`}>✗ No records found</span>
  return <span className={`${styles.badge} ${styles.badgeWarning}`}>? Uncertain</span>
}

function ConfidenceDot({ level }) {
  const cls = level === 'high' ? styles.dotSuccess : level === 'medium' ? styles.dotWarning : styles.dotMuted
  return <span className={`${styles.dot} ${cls}`} />
}

function StatCard({ label, value }) {
  return (
    <div className={styles.statCard}>
      <span className={styles.statLabel}>{label}</span>
      <span className={styles.statValue}>{value ?? '—'}</span>
    </div>
  )
}

function ResultCard({ data }) {
  return (
    <div className={styles.resultCard}>
      <div className={styles.resultHeader}>
        <div>
          <div className={styles.companyName}>{data.company}</div>
          <div className={styles.resultMeta}>
            <ConfidenceDot level={data.confidence} />
            <span className={styles.confidenceLabel}>{data.confidence} confidence</span>
          </div>
        </div>
        <Badge sponsors={data.sponsors} />
      </div>

      <div className={styles.statsRow}>
        <StatCard label="Total petitions" value={data.totalPetitions?.toLocaleString()} />
        <StatCard label="Most recent year" value={data.recentYear} />
        <StatCard
          label="Avg salary"
          value={data.avgSalary ? `$${Math.round(data.avgSalary / 1000)}k` : null}
        />
      </div>

      {data.topRole && (
        <div className={styles.topRole}>
          <span className={styles.topRoleLabel}>Top sponsored role</span>
          <span className={styles.topRoleValue}>{data.topRole}</span>
        </div>
      )}

      <p className={styles.summary}>{data.summary}</p>

      <a
        href={data.h1bDataUrl}
        target="_blank"
        rel="noopener noreferrer"
        className={styles.sourceLink}
      >
        View raw data on h1bdata.info →
      </a>
    </div>
  )
}

export default function App() {
  const [query, setQuery] = useState('')
  const [status, setStatus] = useState(STATUS.IDLE)
  const [result, setResult] = useState(null)
  const [errorMsg, setErrorMsg] = useState('')
  const inputRef = useRef(null)

  async function handleSearch() {
    const company = query.trim()
    if (!company) { inputRef.current?.focus(); return }

    setStatus(STATUS.LOADING)
    setResult(null)
    setErrorMsg('')

    try {
      const res = await fetch(`/api/h1b?company=${encodeURIComponent(company)}`)
      const data = await res.json()

      if (!res.ok || data.error) {
        throw new Error(data.error || `Server error ${res.status}`)
      }
      setResult(data)
      setStatus(STATUS.SUCCESS)
    } catch (err) {
      setErrorMsg(err.message)
      setStatus(STATUS.ERROR)
    }
  }

  function handleKey(e) {
    if (e.key === 'Enter') handleSearch()
  }

  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <div className={styles.pill}>H-1B Sponsorship Lookup</div>
        <h1 className={styles.headline}>
          Does this company<br />sponsor H-1B visas?
        </h1>
        <p className={styles.subline}>
          Powered by DOL public disclosure data via h1bdata.info
        </p>
      </header>

      <main className={styles.main}>
        <div className={styles.searchBox}>
          <input
            ref={inputRef}
            className={styles.searchInput}
            type="text"
            placeholder="e.g. Bloomberg, JPMorgan Chase, Google..."
            value={query}
            onChange={e => setQuery(e.target.value)}
            onKeyDown={handleKey}
            disabled={status === STATUS.LOADING}
            autoFocus
          />
          <button
            className={styles.searchBtn}
            onClick={handleSearch}
            disabled={status === STATUS.LOADING}
          >
            {status === STATUS.LOADING ? (
              <span className={styles.spinner} />
            ) : (
              'Check'
            )}
          </button>
        </div>

        {status === STATUS.LOADING && (
          <div className={styles.loadingState}>
            <div className={styles.loadingDots}>
              <span /><span /><span />
            </div>
            <span>Searching sponsorship records...</span>
          </div>
        )}

        {status === STATUS.ERROR && (
          <div className={styles.errorBox}>
            <span className={styles.errorIcon}>!</span>
            {errorMsg}
          </div>
        )}

        {status === STATUS.SUCCESS && result && (
          <ResultCard data={result} />
        )}

        {status === STATUS.IDLE && (
          <div className={styles.suggestions}>
            {['Bloomberg', 'JPMorgan Chase', 'Stripe', 'Palantir', 'GE HealthCare'].map(c => (
              <button
                key={c}
                className={styles.chip}
                onClick={() => { setQuery(c); setTimeout(handleSearch, 0) }}
              >
                {c}
              </button>
            ))}
          </div>
        )}
      </main>

      <footer className={styles.footer}>
        Data sourced from US Department of Labor LCA disclosures · Not legal advice
      </footer>
    </div>
  )
}
