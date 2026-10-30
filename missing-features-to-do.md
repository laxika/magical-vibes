# Missing features to do

Engine features that don't exist yet, found while fixing card tests. Each entry lists the tests that
fail because the feature is missing, what the rules require, and a ready-to-use prompt that kicks off
the implementation.

To add an entry, copy the template at the bottom, fill it in, and append it above the template.
Remove an entry once the feature has landed and its tests pass.

---

## 67. Ability copies go on the stack before their new targets are chosen

**Status:** open · **Found:** 2026-10-28, copy-spell staging fix (C batch)

**Failing tests:**
- None yet. Add one per retarget handler: while the "choose new targets" prompt for a copied activated or
  triggered ability is pending, the copy must not be on `gd.stack`.

**What the rules require.** CR 707.10c: "Once the player has decided what the copy's targets will be, the
copy is put onto the stack with those targets." This applies to copies of abilities as well as spells. Spell
copies now wait in `GameData.stagedSpellCopies` until the decision is made
(`CopySupport.addCopyToStack(..., retargetPrompt)` and `commitDecidedStagedCopies`, called from the
`InputCompletionService` epilogue). Ability copies still go straight onto the stack first and are then
retargeted there: `CopyAbilityRetargetHandler`, `CopyActivatedAbilityRetargetHandler`,
`CopyTriggeredAbilityRetargetHandler`, the ability path of `CopyTargetSpellOrAbilityEffectHandler`, and the
ability per-target (psychic-battle) prompts. Becomes-target triggers such as ward (CR 702.21a) can
therefore fire for the copy's original targets even when it is retargeted.

**Why it's large.** It spans several copy handlers (Rings of Brighthearth, Strionic Resonator, Lithoform
Engine, Kurkesh and similar) and their retarget handlers. Each retarget lookup must find staged ability
copies, and many existing tests assert the copy on the stack while its prompt is pending (the spell fix
changed about 45 such assertions).

**Prompt to start it:**

```
In magical-vibes, extend the spell-copy staging from CopySupport (stagedSpellCopies,
addCopyToStack with a retargetPrompt, commitDecidedStagedCopies) to copies of activated and triggered
abilities, so an ability copy enters the stack only after its controller decides its targets (CR 707.10c;
verify with get_rule). Cover CopyAbilityRetargetHandler, CopyActivatedAbilityRetargetHandler,
CopyTriggeredAbilityRetargetHandler, the ability path of CopyTargetSpellOrAbilityEffectHandler and the
ability psychic-battle prompts. Commit triggers (ability-copy triggers, becomes-target triggers for the
final targets only) must fire when the copy enters the stack. Add tests (copy absent from the stack while
the prompt is pending; ward on a retargeted-away original target doesn't trigger), update tests that read
the copy from the stack mid-prompt, and run the copy-card regression tests one class at a time.
```

## 68. Triggers from a committed copy are placed between copies created together

**Status:** open · **Found:** 2026-10-28, copy-spell staging fix (C batch)

**Failing tests:**
- None yet. Add one with storm (or another multi-copy effect) plus a "whenever you copy a spell" or ward
  trigger, checking that all copies are on the stack before any of those triggers.

**What the rules require.** CR 603.3: a triggered ability is put on the stack "the next time a player would
receive priority." When one effect creates several copies, no player receives priority between them, so
every copy should be on the stack before any trigger they caused, and those triggers then go on top, in
APNAP order (CR 603.3b). `CopySupport.commitDecidedStagedCopies` (and the immediate `addCopyToStack` path)
calls `checkSpellCopyTriggers` and `checkBecomesTargetOfSpellTriggers` per copy. Any trigger those checks
put onto the stack right away can end up between copies. The pre-staging code did the same per copy.

**Why it's large.** The fix is to queue those triggers as pending until the batch of copies is committed,
then stack them together. That touches how the copy and becomes-target trigger collectors push entries,
and those collectors are shared with ordinary spell casting.

**Prompt to start it:**

```
In magical-vibes, make the triggers caused by copying spells (checkSpellCopyTriggers and
checkBecomesTargetOfSpellTriggers called from CopySupport.addCopyToStack and commitDecidedStagedCopies)
wait until all copies made by the same effect are on the stack, then go on the stack together in APNAP
order (CR 603.3 / 603.3b; verify with get_rule). First write a failing test (e.g. Grapeshot or another
storm card with a "whenever you copy a spell" permanent or a ward target) showing a trigger landing
between copies. Keep ordinary cast-time trigger behavior unchanged; run the copy-card and storm
regression tests one class at a time.
```

---

## 69. The frontend can't activate an "any player may activate" ability on another player's permanent

**Status:** open (backend done 2026-10-30) · **Found:** 2026-10-28, F-card test batch

**What the rules require.** Flailing Soldier and similar permanents (Oona's Prowler and others flagged
`ActivatedAbility.isActivatableByAnyPlayer()`) let any player activate the ability. The backend now accepts an
optional `sourcePermanentId` on `ActivateAbilityRequest` (resolved through `GameService.activateAbility`'s
source-ID overload), so the index ambiguity is gone server-side; the harness uses `activateAbilityOfPermanent`.
The Angular app still only offers activations on the player's own battlefield (`targeting-choice.service.ts`
reads `myBattlefieldFn()`), and the permanent view doesn't expose `activatableByAnyPlayer`.

**Why it's large.** The permanent view needs the flag, opponent permanents need click handling and an
ability menu, and the request has to carry `sourcePermanentId`.

**Prompt to start it:**

```
In magical-vibes, let the Angular frontend activate "any player may activate" abilities (ActivatedAbility
.isActivatableByAnyPlayer()) on permanents the player doesn't control: expose the flag in the permanent view,
offer those abilities when clicking an opponent's permanent, and send ActivateAbilityRequest.sourcePermanentId.
Read agent-docs/ARCHITECTURE.md first (view immutability, networking).
```

---

## 70. Snowfall's "may add" mana needs an optional choice inside a triggered mana ability

**Status:** open · **Found:** 2026-10-30, O/P mixed card-test batch

**Disabled tests:**
- `SnowfallTest` "The Island's controller may decline Snowfall's additional mana"

**What the rules require.** Snowfall's Oracle text says that whenever an Island is tapped for mana, its
controller *may* add an additional {U} (and another if the Island is snow). That is a triggered mana ability
(CR 605.1b): it resolves immediately, without the stack, but the "may" is still a choice for the Island's
controller. `LandTapTriggerCollectorService.handleAddRestrictedManaWhenSubtypeLandTapped` adds the mana with no
choice at all.

**Why it's large.** The engine has no way to ask a question in the middle of a mana ability, because mana
abilities resolve synchronously inside cost payment and auto-tapping. It needs a pending optional-choice path
for triggered mana abilities that also works while a cost is being paid. The other `SnowfallTest` tests
(`opponentIslandAlsoBenefits`, `islandAddsRestrictedBlue`, `snowIslandAddsTwoRestrictedBlue`,
`islandManaPaysCumulativeUpkeep`, `multipleSnowfallsAddSeparateSnowBonuses`, `insufficientManaSacrificesSnowfall`)
assume the mana is added with no prompt. Once the prompt exists they have to answer it, or the engine has to
add the mana by default and only ask when it matters.

**Prompt to start it:**

```
In magical-vibes, support optional choices in triggered mana abilities (CR 605.1b), starting with Snowfall's
"that player may add an additional {U}". LandTapTriggerCollectorService.handleAddRestrictedManaWhenSubtypeLandTapped
currently adds the mana unconditionally. Design how a mana ability can ask its controller a yes/no question,
including while mana is being tapped to pay a cost, then make SnowfallTest "The Island's controller may decline
Snowfall's additional mana" pass and update the other SnowfallTest flows to answer the new prompt. Read
agent-docs/ARCHITECTURE.md first and verify every CR number with the rules MCP.
```

---

## 71. Fluttershy's "creature with a tail" depends on card art

**Status:** open · **Found:** 2026-10-30, mixed card-test batch

**Disabled tests:**
- `FluttershyTest` "Only creatures with tails receive counters"

**What the rules require.** Fluttershy (`SLD` 1538) reads "Put a +1/+1 counter on each creature with a tail
target player controls." Whether a creature has a tail is decided by looking at its art, as with the
Unfinity-style physical-characteristic cards. Scryfall has no ruling and the card data has no field for it. The
engine currently puts a counter on every creature the target player controls.

**Why it's large.** It needs a way to answer art-based questions: either per-card data (a curated "has a
tail" flag, a guess for thousands of cards) or a prompt that lets the players decide each creature. Neither
exists.

**Prompt to start it:**

```
In magical-vibes, decide how art-based characteristics such as Fluttershy's "creature with a tail" should be
answered (curated per-card data or a player prompt), implement it, restrict Fluttershy's counter effect to
creatures with a tail, and re-enable FluttershyTest "Only creatures with tails receive counters". Read
agent-docs/ARCHITECTURE.md first.
```

---

<!--
Template — copy everything between the lines, give it the next number, and append above.

## N. <Short title>

**Status:** open · **Found:** YYYY-MM-DD, <context>

**Failing tests:**
- `<TestClass>.<test>()` (or the display name)

**What the rules require.** <Verified CR text / rulings, and what the engine does instead.>

**Why it's large.** <Scope and regression risk.>

**Prompt to start it:**

```
<A self-contained prompt that can be pasted to kick off the feature.>
```
-->
