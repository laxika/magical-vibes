# Card Info MCP server

This local MCP server gives card-authoring models compact Oracle data from Scryfall with an
automatic MTGJSON fallback, without putting a full provider response into model context.
A lookup downloads **every printing in the requested set**,
stores a compact persistent set cache, and returns only the requested card.

The compact card includes name, set/collector number, layout, mana cost, type line, oracle text,
power/toughness/loyalty/defense, colors, color identity, keywords, and compact card faces.
Each lookup also includes `source` (`scryfall` or `mtgjson`) and, when available for MTGJSON,
`source_date` (the upstream dataset date). It omits
images, prices, legalities, purchase links, preview data, and other fields unused by card
implementation.

## Requirements

- Node.js 20 or newer. There are no npm dependencies and no install step.
- Network access to `https://api.scryfall.com` when a set cache is missing, or
  `https://mtgjson.com/api/v5/<SET>.json` for the fallback.

## MCP mode

From the repository root:

```powershell
./mcp/card-info/start.ps1
```

The process speaks newline-delimited MCP JSON-RPC over stdin/stdout. Project configuration is
included for Codex (`.codex/config.toml`), Claude Code (`.mcp.json`), and Cursor
(`.cursor/mcp.json`). Restart/reload the client after pulling this change; Claude Code will ask you
to approve the project-scoped server the first time.

The server exposes one read-only tool:

- `get_card(set_code, collector_number)`

## Command-line mode

The same implementation is used by the repository's PowerShell card helpers:

```powershell
# Return one compact card; downloads the whole set on a cache miss
./mcp/card-info/start.ps1 get-card DKA 76

# Download/read a whole set and print a short cache summary
./mcp/card-info/start.ps1 cache-set DKA

# Print the complete compact cached set (used by export-set-metadata.ps1)
./mcp/card-info/start.ps1 get-set DKA
```

## Cache

Set files are written to `~/.magical-vibes-mcp/cache/<set>.json` (on Windows,
`%USERPROFILE%\.magical-vibes-mcp\cache\<set>.json`). The directory is created on the first
cache miss and shared by all MCP clients, helpers, and updated repository checkouts for the
same user. It contains compact MCP data and lock/rate-limit files; the rules MCP also stores
`comprehensive-rules.json` here. The Java loader continues using the checkout's `card-data-cache`.

Cache entries never expire. Delete a set's global cache file manually if it must be downloaded
again. Set `CARD_INFO_CACHE_DIR` to relocate the card cache; an override also relocates its
locks and rate limiter. Older checkouts' `mcp/card-info/cache/<set>.json` files can be copied into
the global directory without overwriting existing entries. Restart running MCP clients after
updating so they use the shared default.

Scryfall pages include explicit `User-Agent` and `Accept` headers. A shared request lock spaces
request starts at least 125 ms apart across MCP/helper processes using the same cache directory.
A per-set lock also prevents downloading the same set simultaneously. Traffic from other
applications or different cache directories is outside this limiter.

On HTTP 429/403, network failure, HTTP 5xx, or malformed successful Scryfall responses, the
lookup falls back to MTGJSON immediately instead of asking the model to wait and retry. A
429/403 starts a shared cooldown of at least 60 seconds, extended by `Retry-After` when present;
other lookups use MTGJSON during that cooldown. Ordinary errors such as HTTP 400/404 remain
errors rather than being hidden by fallback. If both providers fail, the error names both.

MTGJSON fallback first reuses `card-data-cache/mtgjson-<set>.json` from the repository's Java
loader, then downloads the set if necessary. It uses English `text` (Oracle text), not
`originalText`, maps fields to the compact schema, and combines multi-face entries. The compact
fallback set is persisted in the same MCP cache, so later lookups do not retry Scryfall.
Existing cache entries without a source are treated as Scryfall. As with existing Scryfall
caches, MTGJSON files can be stale; inspect `source_date` and remove the relevant cache files
when a refresh is needed. Missing MTGJSON printings fail explicitly; no different printing is
silently substituted. `MB1` uses MTGJSON's `CMB1` file, matching the Java loader.

This fallback applies to the MCP and its PowerShell helpers. The Java runtime's configured
Oracle provider behavior is unchanged. Restart existing MCP clients/review workers to load
the new implementation. Codex's tool timeout is 60 seconds to allow both provider attempts.

## Tests

```powershell
& 'C:\Program Files\nodejs\node.exe' --test --test-isolation=none `
  mcp/card-info/test/card-cache.test.mjs mcp/card-info/test/server.test.mjs `
  mcp/card-info/test/fallback.test.mjs mcp/card-info/test/shared-cache.test.mjs
```

If `node` is on `PATH`, this is equivalent:

```powershell
cd mcp/card-info
npm test
```
