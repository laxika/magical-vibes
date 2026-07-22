READ CLAUDE.md FIRST!!!

To implement a card, use the **`implement-card`** skill — it owns the workflow (reprint check, Scryfall lookup, mapping oracle text to effects via `agent-docs/`, writing the card class, and writing focused tests).

Use the configured Scryfall MCP `get_card` tool for extra card lookups; do not pull raw Scryfall card JSON into model context. The server caches the whole requested set and returns only implementation-relevant fields for one card.
