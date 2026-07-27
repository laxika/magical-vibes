---
name: implement-card
description: Implement one or more Magic cards in the magical-vibes engine from a set code and collector number(s) (e.g. "implement DKA 76", "implement SOS 1 2 3 4", "add Tragic Slip"). Handles reprint detection, Scryfall lookup, choosing/reusing effects from agent-docs, writing the card class, and writing focused card tests.
---

# Implement a card

Input is a **set code + one or more collector numbers** from that set (e.g. `DKA 76`, or `SOS 1 2 3 4`). If the user gives a card name instead, ask for, or look up, the set/collector number first.

**Multiple collector numbers:** implement each card independently, one at a time — run Steps 2–7 fully for one collector number before moving to the next. This keeps each card's context small and lets a failing card not block the others. Report a short per-card summary (implemented / reprint / tests pass-fail) at the end.

The hard rules in `CLAUDE.md` (main branch, no commits, rules accuracy, reuse over creation) apply throughout.

## Search discipline (every step)

Context spent searching is context not spent on the card. The card class and its test are ~20–150 lines; `SpellCastingService` alone is ~3,500. Four rules:

- **Never read the same file twice.** `Grep -n` for the line, then one `Read` with `offset`/`limit`. Re-reading a large engine service to "check one more thing" costs more than everything you will write.
- **Use `Grep` / `Glob` / `Read`, not their shell equivalents.** `Select-String`, `Get-Content` and `Get-ChildItem -Recurse` pay a shell spawn, return no clickable references, and invite wide `-Context` windows that drag dozens of irrelevant lines along. Prefer `output_mode: content` with a `head_limit`.
- **One search per question.** A `Grep` and a `Select-String` for the same pattern buy the same answer twice.
- **Change files with `Edit`.** Never rewrite one through a `python` heredoc or shell script — those carry the old text, the new text *and* script boilerplate, where `Edit` carries only the first two. If an `Edit` fails, fix the match string; do not fall back to a script.

## Step 1 — Gather context

Run the helper once. It loads compact Scryfall data (name, mana, type, oracle, P/T, keywords) through the shared Card Info whole-set cache and runs the reprint check. It stays silent about anything you can derive yourself — file paths and the test command are spelled out in Steps 4/6/7 below, and it flags the tests decision only when tests should be **skipped**. Pass every collector number in one call — the script emits a separate, clearly delimited context block per card:

```
bash -c 'powershell.exe -NoProfile -File scripts/implement-card-context.ps1 <SET> <COLLECTOR_NUMBER> [<COLLECTOR_NUMBER> ...]'
```

`-ClassName` is only valid with a single collector number; with several cards each name is derived from Scryfall. The script is a deterministic lookup only — it does **not** decide the implementation. You do that from the docs in Step 3.

For a **multi-face card** (transform, MDFC, split) the script prints every face's oracle text and then stops, because no class name can be derived from a two-faced name. Decide how — or whether — the engine represents that card before going further; re-run with `-ClassName <Name>` to get the reprint check once you have.

For any additional card lookup, use the configured Scryfall MCP `get_card` tool. Never fetch or place raw Scryfall card JSON in model context; the MCP response intentionally omits images, prices, legalities, purchase links, and other fields irrelevant to implementation.

## Step 2 — Reprint short-circuit

If the script reports **EXISTING CLASS FOUND**, this is a reprint: add a single
`@CardRegistration(set = "SET", collectorNumber = "N")` annotation to the existing class and **stop**. Do not implement logic, do not write or run tests.

## Step 3 — Map oracle text to effects (the important part)

Use the docs in this lookup order. **Always `Grep` with keywords from the oracle text — never `Read` these files in full** (they are large):

1. `agent-docs/CARD_PATTERN_INDEX.md` — closest archetype + copy-paste template. Check first.
2. `agent-docs/ORACLE_TEXT_EFFECT_MAP.md` — maps oracle phrases to effect classes + slots.
3. `agent-docs/EFFECTS_QUICK_REFERENCE.md` — compact list of all effects.

Detailed references — grep these when the three above aren't enough:
- `agent-docs/TRIGGER_SLOT_TARGETING.md` — check before implementing a targeted triggered ability.
- `agent-docs/ACTIVATED_ABILITY_GUIDE.md` — `ActivatedAbility` overloads, all 28 `EffectSlot`s, cost effects.
- `agent-docs/PREDICATES_REFERENCE.md` — every `TargetFilter` / `PermanentPredicate` / `StackEntryPredicate` / `PlayerPredicate` composition.
- `agent-docs/EFFECTS_INDEX.md` — full per-effect descriptions and usage notes.
- `agent-docs/CARD_IMPLEMENTATION_PLAYBOOK.md` — canonical patterns, targeting checklist, anti-patterns, new-effect/predicate checklists.

Apply the reuse-over-creation rule from `CLAUDE.md` when choosing effects.

## Step 4 — Write the card class

Create `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/{letter}/{ClassName}.java` extending `Card`, where `{letter}` is the lowercased first letter of the class name.

The constructor contains **only engine logic** — `addEffect()`, `addActivatedAbility()`, `target(...)`, `setNeedsTarget()`, etc. All metadata (name, type, mana, color, subtypes, keywords, P/T, card text) is auto-loaded from Scryfall; never set it. Add the `@CardRegistration` annotation(s). Mirror the structure and comment style of a recent card (e.g. `cards/t/TragicSlip.java`, `cards/t/TorchFiend.java`).

## Step 5 — Only if a new effect is genuinely needed

After confirming no existing effect/combination works — read `agent-docs/ARCHITECTURE.md` first (engine invariants: card freezing, layer system, thread safety, Jackson 3 imports). **Never add a new `instanceof <ConcreteEffect>` outside `service/effect/**` / `service/validate/**` — no build check catches it any more, so it is on you.**

**Trace the reference chain in one pass.** This step is where the context budget goes, and it goes on discovery, not on writing. A new effect almost always has a structural twin already in the engine — divided damage, distributed counters, X-cost spells, modal spells. Name that twin first, then pull its entire chain in a *single* batch of parallel reads: the effect record, its `NormalEffectHandlerBean`, any `EffectResolution` flag it sets, its cast-time branch in `SpellCastingService`, its AI branch, and one card that uses it plus that card's test. Finding those touchpoints one grep at a time is the expensive failure mode — each probe re-covers ground the last one already crossed. If the chain is wide, or you cannot tell where it ends, delegate the trace to an `Explore` subagent and keep only its conclusion (the touchpoint list and the signatures you need) rather than every file it opened.

Register the effect's knowledge in the ONE place that owns it:
1. Add a record implementing `CardEffect` in `magical-vibes-domain/.../model/effect/`.
2. Add resolution as a `@Component` implementing `NormalEffectHandlerBean` (declaring `handledEffect()`) under `service/effect/normalfx/` — NOT via `instanceof` in `GameService`. For a continuous/static effect use a `StaticEffectHandler` under `service/effect/staticfx/` instead (see `agent-docs/STATIC_EFFECT_HANDLERS.md` / `LAYER_SYSTEM.md`).
3. **If the effect is targeted** (a single-`targetId` spell or activated ability), override `targetSpec()` on the effect record to return a non-NONE `TargetSpec` (category + `harmful` flag + optional `PermanentPredicate`) — the declarative interpreter narrows the legal target type; leaving it at `NONE` gives the cast path no type checking (Fireball-burns-a-Plains bug). A `@ValidatesTarget(YourEffect.class)` validator under `service/validate/` is now ONLY an escape hatch for non-structural rules, and still needs the structural spec. See `agent-docs/EFFECTS_INDEX.md` § "Effect targeting declarations" and `agent-docs/TRIGGER_SLOT_TARGETING.md` § "Spell / activated-ability target validation".
4. **If the effect fits an AI-scored family**, implement the matching capability interface in `model/effect/` (`DamageDealingEffect`, `RemovalEffect`, `ManaProducingEffect`, `CardDrawingEffect`, `LifeGainEffect`, `TokenCreatingEffect`, `CreatureBoostEffect`, `StaticCreatureBoostEffect`, `KeywordGrantingEffect`, `ControlStealingEffect`, `CounterSpellingEffect`, `RegenerationEffect`) so the AI reads a FACT, not the type. See `agent-docs/EFFECTS_QUICK_REFERENCE.md` § "Capability / marker interfaces".
5. If Scryfall returns a new subtype/keyword, add it to `CardSubtype` / `Keyword`.
6. If the effect needs a new view flag, update `CardView` / `CardViewFactory` and the frontend `Card` interface in `websocket.service.ts`.
7. **Update the relevant `agent-docs/` files** so the new effect is discoverable.
8. If a service you extended has unit tests, add tests for the new behavior too.

## Step 6 — Write tests (skip per script guidance)

Skip tests only when the script says **basic land** or **vanilla** — it prints nothing here otherwise, which means tests are needed. Add
`magical-vibes-application/src/test/java/com/github/laxika/magicalvibes/cards/{letter}/{ClassName}Test.java` extending `BaseCardTest`.

- Follow the Testing rules in `CLAUDE.md`: behavior through the engine only — never Scryfall-metadata asserts, never white-box wiring tests.
- Use the harness: `setHand`, `addMana`, `addToBattlefield`, `castCreature/castInstant`, `activateAbility`, `passBothPriorities`, `forceStep`, `forceActivePlayer`. See `agent-docs/TEST_RECIPES.md` and `agent-docs/TEST_CREATURES_REFERENCE.md`.
- Typical cases: each resolution branch, "wears off at end of turn" for temporary effects, and an illegal-target rejection.
- Model new tests on a recent sibling test such as `cards/t/TragicSlipTest.java`.

## Step 7 — Run the focused test

Run the quiet wrapper — **never the full suite** (it takes 20+ min) and never the raw `./gradlew` command (its output is noise):

```
bash -c 'powershell.exe -NoProfile -File scripts/run-card-test.ps1 {ClassName}Test'
```

Allow a generous timeout (up to 10 min on a cold build). It prints `PASS …` on success, or each failed test with its assertion message and project stack frames on failure; stale-frontend build failures are retried automatically. Read the full log (`magical-vibes-application/build/card-test.log`) only if the excerpt isn't enough. Report pass/fail honestly. Do not commit unless the user asks.
