# H-1B Sponsorship Checker

Check whether a company has sponsored H-1B visas using DOL public disclosure data (h1bdata.info), powered by Anthropic's API with web search.

## Structure

```
h1b-checker/
├── backend/      Spring Boot API (Java 17)
├── frontend/     React + Vite UI
└── render.yaml   Render deployment config
```

## Local Development

### Backend

```bash
cd backend
export ANTHROPIC_API_KEY=sk-ant-...
mvn spring-boot:run
# API available at http://localhost:8080
```

Test:
```bash
curl "http://localhost:8080/api/h1b?company=Bloomberg"
```

### Frontend

```bash
cd frontend
npm install
npm run dev
# App available at http://localhost:5173
# /api/* proxied to localhost:8080 automatically
```

## Deployment

### Backend → Render

1. Push this repo to GitHub
2. Go to [render.com](https://render.com) → New → Blueprint → connect repo
3. Render will detect `render.yaml` automatically
4. Set these env vars in Render dashboard:
   - `ANTHROPIC_API_KEY` — your Anthropic API key
   - `CORS_ALLOWED_ORIGINS` — your Vercel frontend URL (after step below), e.g. `https://h1b-checker.vercel.app`

### Frontend → Vercel

1. Go to [vercel.com](https://vercel.com) → New Project → import this repo
2. Set **Root Directory** to `frontend`
3. Framework: Vite (auto-detected)
4. Deploy — no env vars needed (proxy in `vercel.json` handles backend URL)
5. Update `frontend/vercel.json` → replace `https://h1b-checker-api.onrender.com` with your actual Render service URL

## API

```
GET /api/h1b?company={name}
```

Response:
```json
{
  "company": "Bloomberg",
  "sponsors": true,
  "totalPetitions": 1240,
  "recentYear": 2025,
  "avgSalary": 165000,
  "topRole": "Software Engineer",
  "summary": "Bloomberg has an active H-1B sponsorship history...",
  "confidence": "high",
  "h1bDataUrl": "https://h1bdata.info/index.php?em=Bloomberg&year=All+Years"
}
```

## Notes

- Data sourced from US DOL LCA disclosures — not legal advice
- Web search costs $10/1000 searches (billed by Anthropic)
- Render free tier spins down after inactivity — first request may be slow (~30s)
