# Prevent* family collapse — session log

## Session 1 (2026-07-18): spec suite + one-shot shield family folded (12 records + 12 handlers deleted)

### De-risk first (committed separately)
- `prevention/DamagePreventionSpecTest` pins the cross-cutting shield-CONSUMPTION semantics
  (global-before-creature ordering, persistence across events, no consumption under full player
  prevention, combat/noncombat windows, one-decrement-per-event color charges,
  `damageCantBePreventedThisTurn` bypass without consumption). Green before AND after the collapse.
- Coverage audit: the 19 shield-creating records had 54 user cards; only `HolyDay` lacked a
  behavioral test → `HolyDayTest` added. Every migrated card is behaviorally pinned.

### What was added
- `PreventDamageEffect(PreventionScope scope, DynamicAmount amount, boolean combatOnly,
  Set<CardColor> sourceColors, PermanentPredicate exemptPredicate)` + compact-ctor pairing
  validation (session-33 `DoesntUntapEffect` precedent) + 15 static factories
  (`nextToAny/nextToController/nextToSelf/nextToTarget(int|DynamicAmount)`, `allCombat`,
  `allToCreatures`, `all[Combat]ToTargetCreatures`, `all[Combat]ByTargetCreatures`,
  `allToControllerAndCreatures`, `allToControllerFromAttackers`, `fromColors`, `allCombatExcept`).
- New `PreventionScope` enum (12 values) — each value maps 1:1 onto the pre-existing shield-state
  slot its old record's handler wrote. **The consumption side (`DamagePreventionService`) is
  untouched**; the collapse is creation-side only.
- ONE `PreventDamageEffectHandler` (switch on scope); every branch is the verbatim body of the old
  per-record handler, logs byte-identical. `NEXT_*` amounts evaluated via `AmountEvaluationService`
  with `AmountContext.forStackEntry(entry, null)` — exactly the old `PreventDamageToTargetEffect`
  handler's context, so `XValue` (Alabaster Potion) keeps working; the old int-only records fold via
  `Fixed` sugar with identical results.
- `targetSpec()` per scope: `NEXT_TO_TARGET` → benign ANY_TARGET (old `PreventDamageToTargetEffect`),
  the two target-creature scopes → benign CREATURE (old records), everything else NONE (old records
  declared none).

### Records deleted (12 + 12 handlers; no handler-unit-tests existed; zero validators / zero AI /
### zero live-instanceof sites outside the handlers — verified by grep + clean compile)
| Old record | Factory | Users |
|---|---|---|
| `PreventNextDamageEffect` | `nextToAny(N)` | Barrenton Medic, Samite Healer |
| `PreventNextDamageToControllerEffect` | `nextToController(N)` | Esper Battlemage |
| `PreventNextDamageToSelfEffect` | `nextToSelf(N)` | Ethereal Champion |
| `PreventDamageToTargetEffect` | `nextToTarget(N \| DynamicAmount)` | 11 cards (Healing Salve, Alabaster Potion X, …) |
| `PreventAllCombatDamageEffect` | `allCombat()` | 8 cards (Fog, Holy Day, …) |
| `PreventAllDamageToAllCreaturesEffect` | `allToCreatures()` | Blinding Fog, Forfend |
| `PreventAllDamageToTargetCreatureEffect` | `all[Combat]ToTargetCreatures()` | Foxfire, Godtoucher, Redeem, Wellgabber Apothecary |
| `PreventAllDamageByTargetCreatureEffect` | `all[Combat]ByTargetCreatures()` | Foxfire, Inquisitor's Snare, Resistance Fighter, Soul Parry |
| `PreventAllDamageToControllerAndCreaturesEffect` | `allToControllerAndCreatures()` | Endure, Safe Passage |
| `PreventAllDamageToControllerFromAttackingCreaturesEffect` | `allToControllerFromAttackers()` | Deep Wood, Heavy Fog |
| `PreventDamageFromColorsEffect` | `fromColors(colors)` | Luminesce |
| `PreventCombatDamageExceptBySubtypesEffect` | `allCombatExcept(pred)` | Moonmist |

Sweep: 38 card files migrated by a CRLF-safe byte-level replace (ctor head → factory + import swap,
duplicate-import dedupe for Foxfire which used two records). Verified: spec suite + all 38 card
tests (214 tests, 0 failures) + the architecture suite (TargetSpec ratchet/consistency,
CardImmutability) all PASS.

### Deliberately left out (and why)
- **Chosen-source shields** — 4 of the 6 folded in session 2 (below). Still out:
  `PreventDamageToTargetFromChosenSourceEffect` (cast-time target + amount + the
  `PreventDamageToTargetFromSourceChoice` context resumes a suspended effect resolution via
  `pendingEffectResolutionEntry` — a different answer-side mechanism) and
  `PreventNextColorDamageToControllerEffect` (chooses a *color*, not a source; color-count shield).
- **`PreventDividedDamageEffect`** — rides the divided-damage cast flow
  (`EffectResolution.needsDamageDistribution` + `damageAssignments`); folding it means threading the
  division machinery through the spec record for one user (Remedy).
- **`PreventNextDamageToTargetAndAddToughnessCountersEffect`** (Sacred Boon) — writes the separate
  `damageToCounterPreventionShield` with a delayed-counter rider; an And-rider, not a pure shield.
- **All static always-on markers with riders** (Vigor, Purity, Urza's Armor, Battletide Alchemist,
  Hostility, Swans, Immortal Coil, Dolmen Gate, Guardian Seraph, Shield of the Realm, Prismatic
  Ward, the to-and-by enchanted-creature aura pair, Magebane Armor) and the redirect/reflection
  family (Harm's Way, Eye for an Eye, Vengeful Archon, Oracle's Attendants, Jade Monolith) — each
  is live-read at damage time with genuinely distinct rider logic in
  `DamagePreventionService`/`DamageSupport`; collapsing the records would not remove those read-site
  branches (the roadmap's original "high risk" assessment applied to THESE, not to the one-shot
  shield spells folded here).

### Metrics
- Prevent* records: 41 → 29 (−12); handlers: 22 → 11 (−12 deleted, +1 added).
- The remaining 29 split: 6 chosen-source/divided/Sacred-Boon one-shots (future scope axes),
  ~14 static markers with riders, the rest redirect/reflection records.

## Session 2 (2026-07-18): chosen-source subfamily

`PreventNextDamageFromChosenSourceEffect(gainLife)`, `…MatchingEffect(filter, label)`,
`…ToAnyTargetEffect()` and `PreventAllDamageFromChosenSourceEffect(controllerOnly, colorFilter)`
(4 records + 4 handlers) collapsed onto
`PreventDamageFromChosenSourceEffect(ChosenSourcePreventionScope scope, boolean gainLife,
boolean controllerOnly, PermanentPredicate sourceFilter, String sourceLabel)` with 6 static
factories (`nextDamageToYou[AndGainLife]`, `nextDamageToYou(filter, label)`,
`nextDamageToAnyTarget`, `allDamageToYou`, `allDamage(filter, label)`) + ONE handler.

- **Creation-side only again**: the three answer-side `PermanentChoiceContext` variants and their
  `PermanentChoiceBattlefieldHandlerService` handlers (shield stores
  `playerSourceNextDamageShields` / `sourceNextDamageToAnyTargetShields` /
  `playerSourceDamagePreventionIds` / `permanentsPreventedFromDealingDamage`) are untouched; the
  merged handler just picks the right context + prompt per scope. All prompts byte-identical
  (including the pre-existing "Choose a artifact source." article quirk — kept, cosmetic).
- Burrenton Forge-Tender's `Set<CardColor>` colorFilter folded into the generic
  `sourceFilter`/`sourceLabel` axes (`PermanentColorInPredicate` + "red");
  `PermanentChoiceContext.PreventDamageSourceChoice` lost its dead `colorFilter` field (the answer
  handler never read it) and its unused 1-arg ctor.
- Zero validators / zero AI / zero live-instanceof / zero test references to the old records
  (grep-verified) — pure record+handler+card sweep (12 cards: Reverse Damage, Pentagram of the
  Ages, CoP White/Red/Green/Blue/Black/Artifacts, Greater Realm of Preservation, Sanctum Guardian,
  Auriok Replica, Burrenton Forge-Tender). All 12 behavioral card tests + DamagePreventionSpecTest
  green.
- Records now 26 (−4 +1), handlers 8 (−4 +1) in the family.
