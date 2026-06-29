READ CLAUDE.md FIRST!!!

To implement a card, use the **`implement-card`** skill — it owns the workflow (reprint check, Scryfall lookup, mapping oracle text to effects via `agent-docs/`, writing the card class, and writing focused tests).

The skill runs `powershell -ExecutionPolicy Bypass -File scripts/implement-card-context.ps1 <SET_CODE> <COLLECTOR_NUMBER> [ClassName]`, a deterministic helper that prints:
- Scryfall summary (name, mana, type, oracle, P/T, keywords)
- Reprint check — if the class already exists, just add a `@CardRegistration` annotation for the new printing and stop
- Test guidance (skip for basic land / vanilla / reprint, otherwise write focused tests)
- Suggested card/test file paths and the focused Gradle test command

Pass `-Reference <ClassName>[,<ClassName>]` to also dump those reference cards' constructors inline (and their test paths) so you don't have to read the whole files. Pick references from `agent-docs/CARD_PATTERN_INDEX.md`.
