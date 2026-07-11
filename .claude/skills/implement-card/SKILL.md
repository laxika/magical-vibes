---
name: implement-card
description: Implement one or more Magic cards in the magical-vibes engine from a set code and collector number(s) (e.g. "implement DKA 76", "implement SOS 1 2 3 4", "add Tragic Slip"). Handles reprint detection, Scryfall lookup, choosing/reusing effects from agent-docs, writing the card class, and writing focused card tests.
---

# Implement a card

Input is a **set code + one or more collector numbers** from that set (e.g. `DKA 76`, or `SOS 1 2 3 4`). If the user gives a card name instead, ask for, or look up, the set/collector number first.

**Multiple collector numbers:** implement each card independently, one at a time — run Steps 2–7 fully for one collector number before moving to the next. This keeps each card's context small and lets a failing card not block the others. Report a short per-card summary (implemented / reprint / tests pass-fail) at the end.

Follow the project's hard rules from `CLAUDE.md`:
- Stay on the `main` branch. **Do not commit** until the user explicitly asks.
- **Rules accuracy is the top priority.** If anything about the card's behavior is ambiguous, search the web for the official ruling — never ship rules-incorrect behavior.
- **Reuse over creation.** Prefer combining existing effects, or adding predicates/parameters to existing effects, over writing a new effect class. Only create a new effect when no combination of existing ones works.

## Step 1 — Gather context

Run the helper once. It fetches Scryfall (name, mana, type, oracle, P/T, keywords), runs the reprint check, decides whether tests are needed, and prints suggested file paths + the test command. Pass every collector number in one call — the script emits a separate, clearly delimited context block per card:

```
bash -c 'powershell.exe -NoProfile -File scripts/implement-card-context.ps1 <SET> <COLLECTOR_NUMBER> [<COLLECTOR_NUMBER> ...]'
```

Once you've picked a likely reference card (see Step 3), re-run with `-Reference` to dump its constructor and test path inline instead of reading the whole file (a single card at a time is best here):

```
... implement-card-context.ps1 <SET> <COLLECTOR_NUMBER> -Reference Opt,Shock
```

`-ClassName` is only valid with a single collector number; with several cards each name is derived from Scryfall. The script is a deterministic lookup only — it does **not** decide the implementation. You do that from the docs in Step 3.

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

Reuse rules (from `CLAUDE.md`):
- Recreate effects by combining existing ones where possible (e.g. "2 damage to any target and 3 to you" = `DealDamageToAnyTargetEffect` + `DealDamageToPlayersEffect(3, DamageRecipient.CONTROLLER)`).
- Prefer parameterizing an existing effect with a predicate over a new class (e.g. `DestroyTargetPermanentEffect` + a `PermanentPredicate`, not `DestroyTargetArtifactEffect`).

## Step 4 — Write the card class

Create `magical-vibes-card/src/main/java/.../cards/{letter}/{ClassName}.java` extending `Card`.

The constructor contains **only engine logic** — `addEffect()`, `addActivatedAbility()`, `target(...)`, `setNeedsTarget()`, etc. All metadata (name, type, mana, color, subtypes, keywords, P/T, card text) is auto-loaded from Scryfall; never set it. Add the `@CardRegistration` annotation(s). Mirror the structure and comment style of a recent card (e.g. `cards/t/TragicSlip.java`, `cards/t/TorchFiend.java`).

## Step 5 — Only if a new effect is genuinely needed

After confirming no existing effect/combination works:
1. Add a record implementing `CardEffect` in `magical-vibes-domain/.../model/effect/`.
2. Add resolution logic in `GameService.resolveStackEntry()` (instanceof dispatch).
3. If Scryfall returns a new subtype/keyword, add it to `CardSubtype` / `Keyword`.
4. If the effect needs a new view flag, update `CardView` / `CardViewFactory` and the frontend `Card` interface in `websocket.service.ts`.
5. **Update the relevant `agent-docs/` files** so the new effect is discoverable.
6. If a service you extended has unit tests, add tests for the new behavior too.

## Step 6 — Write tests (skip per script guidance)

Skip tests only when the script says **basic land** or **vanilla**. Otherwise add
`magical-vibes-application/src/test/java/.../cards/{letter}/{ClassName}Test.java` extending `BaseCardTest`.

- Test **engine logic only** by observing **behavior through the engine**: effects, abilities, targeting, interactions. **Never** assert Scryfall metadata (name/type/mana/color/P-T/subtypes/keywords), and **never** write white-box "wiring" tests (e.g. `hasCorrectProperties`) that inspect `card.getEffects(...)`, `EffectSlot`, `EffectResolution.needsTarget`, or effect fields by reflection — resolve the card and assert the outcome instead.
- Use the harness: `setHand`, `addMana`, `addToBattlefield`, `castCreature/castInstant`, `activateAbility`, `passBothPriorities`, `forceStep`, `forceActivePlayer`. See `agent-docs/TEST_RECIPES.md` and `agent-docs/TEST_CREATURES_REFERENCE.md`.
- Typical cases: each resolution branch, "wears off at end of turn" for temporary effects, and an illegal-target rejection.
- Model new tests on a recent sibling test such as `cards/t/TragicSlipTest.java`.

## Step 7 — Run the focused test

Run the single test class the script printed — **never the full suite** (it takes 20+ min):

```
./gradlew :magical-vibes-application:test --tests "com.github.laxika.magicalvibes.cards.{letter}.{ClassName}Test" -x :magical-vibes-frontend:buildAngular
```

If frontend assets are stale and the build fails on that, drop the `-x` flag. Report pass/fail honestly with the output. Do not commit unless the user asks.
