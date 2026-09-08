# Rules Info MCP server

This local MCP server returns verbatim text from the *Magic* Comprehensive Rules,
looked up by rule number. It exists so that a rule number written into the code — `// CR
120.1a`, a Javadoc reference, a commit message — can be checked against the current rules instead
of trusted from memory. **Rule numbers drift:** rules get merged, split, and renumbered every
release, so a number that was right a year ago may now point at a different rule or at nothing.

For example, `120.1b` existed in older releases and does not exist in the rules effective
June 19, 2026. Asking for it fails loudly and names the rules that are actually there:

```
Rule 120.1b does not exist in the Comprehensive Rules effective June 19, 2026. Do not cite it.
Rules that do exist nearby: 120.1, 120.1a
```

## Requirements

- Node.js 20 or newer. There are no npm dependencies and no install step.
- Network access to `https://magic.wizards.com` and `https://media.wizards.com` when the cache is
  missing or older than seven days.

## MCP mode

From the repository root:

```powershell
./mcp/rules-info/start.ps1
```

The process speaks newline-delimited MCP JSON-RPC over stdin/stdout. Project configuration is
included for Codex (`.codex/config.toml`), Claude Code (`.mcp.json`), and Cursor
(`.cursor/mcp.json`) under the server name `rules`. Restart/reload the client after pulling this
change; Claude Code will ask you to approve the project-scoped server the first time.

Two read-only tools:

- `get_rule(rule_ids)` — verify up to 25 rule numbers in one call. Each result is either
  `{rule_id, verified: true, text, subrules, effective_date}` or
  `{rule_id, verified: false, error}`. One bad number does not fail the others.
- `search_rules(query, limit?)` — find rule numbers whose text contains a phrase, with a snippet
  around the hit. This is the follow-up when a number fails verification.

Rule numbers are accepted in the forms an agent is likely to have written: `120.1a`, `CR 120.1A`,
`rule 509.1.`. A rule's text includes its `Example:` lines and any continuation paragraph, because
both are part of the official rule.

## Command-line mode

```powershell
# Verify one or more rule numbers
./mcp/rules-info/start.ps1 get-rule 120.1a 702.11b

# Find the rule number for a phrase
./mcp/rules-info/start.ps1 search "state-based actions are checked"

# Show the cached release, or force a re-download
./mcp/rules-info/start.ps1 status
./mcp/rules-info/start.ps1 refresh
```

## Cache

The rules page at <https://magic.wizards.com/en/rules> is scraped for the dated
`MagicCompRules <date>.txt` link (the URL changes with every release), the text file is split by
rule number, and the result is written to `mcp/rules-info/cache/comprehensive-rules.json`, which is
ignored by Git.

The cache is refreshed when it is **older than seven days**. If Wizards is unreachable at that
point the expired cache is still served, tagged `stale` with the refresh error, since outdated rules
text beats no rules text. Set `RULES_INFO_CACHE_DIR` to relocate the cache. Concurrent processes
coordinate through a lock file so they do not download at the same time.

## Tests

```powershell
& 'C:\Program Files\nodejs\node.exe' --test --test-isolation=none `
  mcp/rules-info/test/rules-cache.test.mjs mcp/rules-info/test/server.test.mjs
```

If `node` is on `PATH`, this is equivalent:

```powershell
cd mcp/rules-info
npm test
```
