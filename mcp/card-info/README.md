# Card Info MCP server

This local MCP server gives card-authoring models compact Scryfall data without putting a full
Scryfall response into model context. A lookup downloads **every printing in the requested set**,
stores a compact persistent set cache, and returns only the requested card.

The compact card includes name, set/collector number, layout, mana cost, type line, oracle text,
power/toughness/loyalty/defense, colors, color identity, keywords, and compact card faces. It omits
images, prices, legalities, purchase links, preview data, and other fields unused by card
implementation.

## Requirements

- Node.js 20 or newer. There are no npm dependencies and no install step.
- Network access to `https://api.scryfall.com` when a set cache is missing.

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

Set files are written to `mcp/card-info/cache/<set>.json` and ignored by Git. Cache entries never
expire. Delete a set's cache file manually if it must be downloaded again. Set
`CARD_INFO_CACHE_DIR` to relocate the cache.

Scryfall pages are requested without application-level pacing and include explicit `User-Agent`
and `Accept` headers. Concurrent processes coordinate through a per-set lock so they do not
download the same set simultaneously.

## Tests

```powershell
& 'C:\Program Files\nodejs\node.exe' --test --test-isolation=none `
  mcp/card-info/test/card-cache.test.mjs mcp/card-info/test/server.test.mjs
```

If `node` is on `PATH`, this is equivalent:

```powershell
cd mcp/card-info
npm test
```
