# Refactors to do

Refactor opportunities spotted during other work, deferred so they don't bloat unrelated changes.
Each entry describes the problem, why it matters, the proposed direction, and a ready-to-use prompt
that kicks off the refactor.

To add an entry, copy the template at the bottom, fill it in, and append it above the template.
Remove an entry once its refactor has landed.

---

## 1. Unify library-search bookkeeping behind one choke point

**Status:** open · **Found:** 2026-10-10, while fixing the Druid of the Emerald Grove hang

**Problem.** `LibrarySearchTriggerHelper.recordSearchAndQueueTriggers` (records the search in
`playersWhoSearchedLibraryThisTurn` and queues "whenever ... searches a library" triggers) is
called from ~37 sites across ~24 engine files. The class Javadoc claims a "unified library-search
choke point", but there isn't one: the shared `LibrarySearchSupport` flow handles the normal case,
while many handlers have early exits (empty library, no matching cards, search prevented, "up to
X" with X = 0) and bespoke per-card handlers (Intuition, Transmute, Emergent Ultimatum, Turtles
Forever, ...) that must each remember to record the search, fire triggers, and shuffle.

**Why it matters.**
- Missed bookkeeping on any exit path silently breaks search-trigger cards and "searched this
  turn" conditions. Concrete example: `SearchLibraryForUpToTwoBasicLandsThenRollD20EffectHandler`'s
  no-match branch logged "Library is shuffled" but never recorded the search, fired triggers, or
  shuffled (and its stale-index insert caused an infinite loop).
- Double-firing is possible if a handler calls the helper and then also enters the shared flow.
- The shuffle that accompanies a search is coupled just as loosely.

**Proposed direction.** One entry point (e.g. in `LibrarySearchSupport`) that owns the whole
search lifecycle and always performs record + triggers + shuffle regardless of outcome (cards
found, none found, empty library, X = 0). Handlers supply only the candidate filter and what to
do with the chosen cards; no handler returns before reaching it. Prevention ("can't search")
is handled in the same place. `recordSearchAndQueueTriggers` becomes private to that service.

**Prompt to start it:**

```
Refactor library-search bookkeeping into a single choke point (see refactors-to-do.md, entry 1).
Today LibrarySearchTriggerHelper.recordSearchAndQueueTriggers is called from ~37 sites; many
search handlers have early exits (empty library, no matches, search prevented, X = 0) or are
bespoke per-card handlers that each have to remember to record the search, queue search
triggers, and shuffle.

1. Inventory every caller of recordSearchAndQueueTriggers and every LibraryShuffleHelper.shuffleLibrary
   call tied to a search; classify each path (normal choice, no match, empty library, prevented,
   bespoke card handler). Also find search handlers that never call it (missed-trigger bugs) and
   paths that could call it twice (double-trigger bugs). Write the inventory to a scratchpad file.
2. Design one entry point in LibrarySearchSupport that owns the lifecycle (prevention check,
   record + triggers, shuffle) for every outcome, and route all handlers through it. Make the
   helper method private/internal afterwards. Read agent-docs/ARCHITECTURE.md first.
3. Rules accuracy first: verify via the rules MCP how searching a library, failing to find, and
   shuffling work before citing any CR number; check official rulings for cards like Aven
   Mindcensor, Stranglehold, Ob Nixilis Unshackled, Archive Trap, and "up to X" with X = 0.
4. Add or extend tests for every outcome path (found / none found / empty library / prevented /
   X = 0) including opponent-search triggers, and list every engine file changed.
Do not run the full test suite; ask me to run it. Don't commit until I say so.
```

---

## 2. Explicit "a creature" vs "another creature" for creature-death triggers

**Status:** open · **Found:** 2026-10-10, while fixing Dark Prophecy / Dread Tiller / Dreadhound

**Problem.** `ON_ALLY_CREATURE_DIES` and `ON_ANY_CREATURE_DIES` serve both "whenever a creature
dies" and "whenever another creature dies" cards. `TriggerCollectionService` now decides
self-inclusion by a heuristic (printed noncreature sources include themselves, printed creatures
don't), and printed creatures that include themselves add a duplicate `ON_DEATH` effect
(Blood Artist / Zulaport / Dread Tiller pattern).

**Why it matters.** About 20 printed creatures whose oracle includes their own death don't trigger
on it today (e.g. Indebted Samurai, Knucklebone Witch, Titania, Nature's Force, Skyclave Shadowcat,
Meltstrider Eulogist, Tributary Instructor, Shadow Urchin, Kheru Bloodsucker, Yarus, Blowfly
Infestation-style any-creature cards). The heuristic would be wrong for a printed noncreature
"another creature" card. The classification of the 181 slot users is in the session scratchpad
(ally_dies_classification.txt) and should be regenerated.

**Proposed direction.** Make self-inclusion explicit per registration (a flag or separate
self-inclusive slots, mirroring `ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD`), migrate cards by
oracle wording, and drop the duplicate `ON_DEATH` workarounds and the printed-type heuristic.

**Prompt to start it:**

```
Refactor creature-death trigger self-inclusion (see refactors-to-do.md, entry 2). Classify every
card on ON_ALLY_CREATURE_DIES / ON_ANY_CREATURE_DIES (and cards with an extra ON_DEATH copy of the
same trigger) by oracle wording via the scryfall MCP: "a/each creature" (includes itself, with
CR 603.10a look-back) vs "another creature". Add an explicit self-inclusive registration (mirror
ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD), migrate the cards, remove the printed-type heuristic
in TriggerCollectionService.checkAllyCreatureDeathTriggers and the duplicate ON_DEATH workarounds,
and add tests for a creature dying itself, an animated noncreature source dying, and "another"
cards not triggering on themselves. Verify every CR number with the rules MCP. Don't run the full
suite; ask me. Don't commit until I say so.
```

---

## 3. Small cleanups left by the D-card test fixes

**Status:** open · **Found:** 2026-10-10

- Discord, Lord of Disharmony's copy ability is now queued in `SpellCastingService`; the old
  collector path for it is dead code — remove it.
- Driftgloom Coyote added a narrow last-known-power special case in
  `EffectResolutionService.isConditionMet`; replace it with a general last-known-information
  aware `TargetPermanentMatches` evaluation.
- `SourceBecomesSubtypeUntilEndOfTurnEffect` Javadoc still says it sets
  `transientCreatureTypeOverride` (it uses a floating `GrantSubtype`).
- Soulbond code cites "CR 702.94" in several files (CreatureControlService, SoulbondSupport,
  ConditionEvaluationService); soulbond is 702.95 — re-verify and fix.
- About 12 cards create Mutagen tokens with `List.of()` subtypes instead of `CardSubtype.MUTAGEN`.
- `TurnCleanupService` still reads raw card STATIC effects in several places, so permanents that
  lost their abilities are still honoured there.

**Prompt to start it:**

```
Do the small cleanups in refactors-to-do.md, entry 3. Verify every CR number with the rules MCP
before writing it. Run only the affected card test classes (ask me for anything broader).
Don't commit until I say so.
```

---

<!--
Template — copy everything between the lines, give it the next number, and append above.

## N. <Short title>

**Status:** open · **Found:** YYYY-MM-DD, <context in which it was spotted>

**Problem.** <What's wrong / duplicated / fragile, with concrete file and method names.>

**Why it matters.** <Bugs it caused or could cause; concrete examples.>

**Proposed direction.** <Target design, kept short.>

**Prompt to start it:**

```
<A self-contained prompt that can be pasted to kick off the refactor.>
```
-->
