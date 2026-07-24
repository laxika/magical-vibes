- Do not start committing changes until I tell you to do so.
- In commit messages and descriptions, never write out full card set names — refer to a set only by its short set code (e.g. `INR`).
- You should always work on the main branch!
- Rules accuracy is the number one priority — never ship rules-incorrect engine behavior. If a card's behavior is at all ambiguous, search the web for the official ruling.
- Reuse over creation: build effects by combining existing ones (e.g. "2 damage to any target and 3 to you" = `DealDamageToAnyTargetEffect` + `DealDamageToPlayersEffect(3, DamageRecipient.CONTROLLER)`) and parameterize with predicates (`DestroyTargetPermanentEffect` + a `PermanentPredicate`, not `DestroyTargetArtifactEffect`) rather than adding new classes. When you do add a new effect/predicate, update the relevant `agent-docs/` files.
- If unit tests exist for a service you extend, add tests for the new behavior too.
- Do not ever run the full test suite! Ask me and I'll run it for you.

## Implementing cards

Use the **`implement-card`** skill (`/implement-card <SET> <COLLECTOR_NUMBER>`). It owns the full workflow — reprint check, Scryfall lookup, mapping oracle text to effects via `agent-docs/` (grep, never read in full), writing the card class, and writing focused tests. Reprints just get a new `@CardRegistration` annotation and nothing else.

Use the configured Scryfall MCP `get_card` tool for any extra card lookup; never put raw Scryfall card JSON into model context. It caches the whole requested set and returns only implementation-relevant fields for the requested printing.

## Architecture

Gradle multi-module CCG (WebSocket real-time multiplayer, Angular frontend). What card work needs:

- Card classes are `Card` subclasses in `magical-vibes-card` (`cards/{letter}/`), annotated `@CardRegistration(set, collectorNumber)`. Constructors contain **only engine logic** (effects, abilities, targeting) — all metadata (name, type, mana, color, P/T, subtypes, keywords, text) is auto-loaded from Scryfall; never set it.
- Effects are records implementing `CardEffect` (`magical-vibes-domain`, `model/effect/`), resolved by `GameService.resolveStackEntry()` (`magical-vibes-engine`) via instanceof dispatch.
- **Before changing anything beyond a card class + its test** (new effect/predicate, service change, static/continuous effect, new `Card` field, networking/frontend), read `agent-docs/ARCHITECTURE.md` — module map plus the invariants engine changes must respect (card freezing, CR 613 layer system, thread safety, Jackson 3 imports, view immutability).

## Testing

Card tests live in `magical-vibes-application/src/test/java/.../cards/{letter}/CardNameTest.java` and extend `BaseCardTest` (JUnit 5 + AssertJ; harness in `magical-vibes-engine/src/testFixtures`). Never assert Scryfall-loaded metadata (name, type, mana, color, P/T, subtypes, keywords) — test only engine logic (effects, abilities, targeting, interactions). Do NOT write white-box "wiring" tests (commonly named `hasCorrectProperties`) that inspect a freshly-constructed card's `getEffects(...)`, `EffectSlot`, `EffectResolution.needsTarget`, or effect field values by reflection — assert behavior by resolving the card through the engine (`harness`/`gs`/`gd`) instead.
