# Missing features to do

Engine features that don't exist yet, found while fixing card tests. Each entry lists the tests that
fail because the feature is missing, what the rules require, and a ready-to-use prompt that kicks off
the implementation.

To add an entry, copy the template at the bottom, fill it in, and append it above the template.
Remove an entry once the feature has landed and its tests pass.

---

## 62. Krark's Thumb result choice for interleaved, nested and non-resolution coin flips

**Status:** partial · **Found:** 2026-10-25, while finishing entry 46 (Krark's Thumb result choice)

**Failing tests:** none yet; a Krark's Thumb test with mixed mocked flips on any remaining path below
would show the heads result kept without a prompt.

**What the rules require.** Krark's Thumb: "If you would flip a coin, instead flip two coins and
ignore one." Its controller sees the results and chooses which to ignore (a replacement effect,
CR 614.1a; CR 705.3 covers effects that fix a flip's result).

**Progress (2026-10-27).** `CoinFlipService` now journals every flip made while the choice window is
open (`GameData.recordedCoinFlips` with a `replayedCoinFlips` cursor; `RecordedCoinFlip` carries the
flipping player and any Pokey d20 roll), so a resolved-again handler replays several flip events and
the flipping player (not always the controller) is asked. New marker
`ChoosesCoinFlipResultsAtCheckpoints`: such a handler calls `CoinFlipService.checkpoint(gameData,
effect, resumePoint)` after each change, which clears the journal, stores a resume point
(`resumePoint`) and reopens the window only for the resolving effect itself
(`GameData.coinFlipCheckpointEffect`). `flipEach` flips several players' coins under one window. A
flip never stops the effect while another prompt is pending. Converted: `FlipUntilLoseEffectHandler`,
`FlipUntilLosePutCountersOnSourceEffectHandler`, `FlipCoinsUntilLoseEffectHandler`,
`ManaClashEffectHandler`, `FlipCoinForEachOpponentEffectHandler`,
`FlipCoinForEachBlockingCreatureEffectHandler` (checkpoints); `EachPlayerFlipsCoinAndTailsSacrificesCreatureEffectHandler`
(flips every player first via `flipEach`), `FlipUntilLoseOrStopEffectHandler` (Fiery Gambit's first
flip) and `RiskyMoveEffectHandler` (when no creature/opponent choice is needed) marked
`ChoosesCoinFlipResultsFirst`. Tests in `KrarksThumbTest` (Crazed Firecat, Mana Clash incl. opponent's
Thumb, Goblin Assassin, Fiery Gambit).

**Progress (2026-10-27, second pass).** Answer completions that flip before changing anything else now
stop for the choice too: `CoinFlipService.answerChoosingResultsFirst(gameData, completion, replay)` opens the
window, stores a `GameData.CoinFlipChoiceReplay` (`MayAnswerReplay` / `InteractionAnswerReplay`, now also
implementing it) in `pendingCoinFlipChoiceReplay` on a stop, and `completeAgainAfterResultChoices` (called by
`ChoiceHandlerService` after the last kept result) completes the same answer again; `replaysRecordedFlips` lets a
pre-flip log line skip the rerun. `InputCompletionService` epilogues close the window. Converted: Fiery Gambit's
"flip again" (`FlipUntilLoseOrStopContinuationHandler`), Game of Chaos (spell handler marked
`ChoosesCoinFlipResultsFirst`; `GameOfChaosFlipAgainHandler` wraps its Lich's Duel Mastery rerun), Risky Move
after its creature/opponent choice (replayed as a `PermanentChosen` answer), Amulet of Quoz (empty-library flip
marked; declined-ante flip wrapped). Crooked Scales's paid repeat already resolves through the stack window and
asks. Tests in `KrarksThumbTest` for each.

**Progress (2026-10-27, third pass).** Audit: every other handler marked only `ChoosesCoinFlipResultsFirst` makes a
single coin-flip call (`flip`/`flipCoins`) before changing anything, so none needs checkpoints. Coin-flip costs:
`CoinFlipCostSupport.pay` now flips every coin first via `flipEach` and only then logs and triggers, and the accepted
optional `FlipCoinsCost` answer in `MayPenaltyChoiceHandlerService` runs inside `answerChoosingResultsFirst` (replayed
as a `MayAnswerReplay`), so each disagreeing flip of a flip-a-coin cumulative upkeep (Karplusan Minotaur) is asked
about before any flip is logged or triggers. Desperate Gambit's flip after its source choice is wrapped the same way
(replayed as a `PermanentChosen` answer). Tests in `KrarksThumbTest`.

**What remains** (these still keep heads when the physical flips disagree):
- nested flips: flips of effects a handler dispatches (payloads of flip-until-lose, per-opponent
  branches, etc.) see a closed window;
- the non-optional `FlipCoinsCost` path resolved directly in `ForcedCostOrElseEffectHandler` (no card uses it today:
  cumulative upkeep's flip cost is always optional);
- `FlipCoinOnEnterEffectHandler` (Molten Sentry's "as enters" flip, applied while the permanent enters) and
  `D20RollService`'s Pokey coin flips. These need their own pause points.

**Prompt to continue it:**

```
Continue missing-features-to-do.md entry 62: extend the Krark's Thumb result choice (journaled
CoinFlipService flips, ChoosesCoinFlipResultsAtCheckpoints + CoinFlipService.checkpoint/resumePoint,
answerChoosingResultsFirst + CoinFlipChoiceReplay for answer completions, CoinFlipChoicePending) to the
remaining paths listed in the entry: nested flips, entry flips (Molten Sentry) and D20RollService. Keep replayed flips from repeating logs or triggers. Add Krark's Thumb tests
with mocked mixed flips for each. Verify every CR number with the rules MCP. Don't run the full test suite;
ask me. Don't commit until I say so.
```

---

## 63. Declining "up to one target player" in single-target and graveyard-mixed trigger prompts

**Status:** open · **Found:** 2026-10-26, while finishing entry 48 (declining optional player groups)

**Failing tests:**
- None yet; found by reading the code. Write a test first (a single-group "up to one target player"
  trigger whose controller declines).

**What the rules require.** "Up to one target" lets the controller choose zero targets for that
instance of "target" (CR 601.2c, applied to triggered abilities by CR 603.3d). Entry 48 gave the
slot-by-slot walker (`ETBTokenTargetService.processNextETBTokenMultiTargetTrigger`) an empty-answer
decline for optional groups that can target the controller. Two prompts still treat "choose
yourself" as both "decline" and "target myself": the single-target `ETBTokenTargetTrigger`
(`ETBTokenTargetService.controllerChoiceDeclines` only declines when the controller is not a legal
target, so an optional "up to one target player" trigger can't be declined), and walker groups that
mix player targets with graveyard or exiled-card candidates (they skip the `EtbPlayerTargetGroup`
prompt).

**Why it's large.** The single-target prompt is a `PermanentChoice` shared by many trigger types
and the AI; it needs a distinct decline answer (or routing through a multi-choice prompt) and the
tests and AI that answer it must follow.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 63: let the controller decline an optional ("up to one")
trigger target that they could also legally target themselves, in the single-target
ETBTokenTargetTrigger prompt and in walker groups that mix players with graveyard/exiled cards.
Find a card with a single "up to one target player" trigger, write a failing decline test, then
give the prompt a decline answer distinct from choosing the controller (see how entry 48 used
MultiPermanentChoiceContext.EtbPlayerTargetGroup). Update the AI and harness answering code.
Verify every CR number with the rules MCP. Don't run the full test suite; ask me. Don't commit
until I say so.
```

---

## 64. Own library top hidden while a temporary grant opens another player's library

**Status:** open · **Found:** 2026-10-26, while finishing entry 49 (playing from any player's library)

**Failing tests:**
- none yet (write one: Future Sight plus Xanathar, Guild Kingpin's grant on the opponent's library)

**What the rules require.** Each permission works on its own library: Future Sight still lets its
controller play the top card of their own library while Xanathar lets them play the top card of the
target opponent's library, so both cards should be playable. `CastingPermissionService.findLibraryTopOwners`
only adds the player's own library when no temporary grant opens a nonempty library (and no
any-library permission applies), so the own top card is not offered. Its top-library permission checks
(`canCastFromTopOfLibrary`, `canCastFromTopOfLibraryNormally`, `hasAnyLibraryTopPermission`, the free
and life play permissions) are not library-aware either, so simply adding the own library could let a
permission meant for one library authorize the other's card.

**Why it's large.** Every top-library permission check needs a library owner (own-library sources like
Future Sight, Courser of Kruphix, Bolas's Citadel vs. grants on a specific library), through the
projection, the cast and land paths, and the AI's playability query.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 64: make the top-library permission checks in
CastingPermissionService library-aware (own-library permissions only for the player's own library,
grants only for their granted library, any-library permissions for all), then let
findLibraryTopOwners include the player's own library alongside temporarily granted ones. Add a test
with Future Sight plus Xanathar, Guild Kingpin showing both top cards are offered and playable, and
keep WindriddlePalacesTest, XanatharGuildKingpinTest, FutureSightTest, CourserOfKruphixTest and
BolassCitadelTest passing. Verify every CR number with the rules MCP. Don't run the full test suite;
ask me. Don't commit until I say so.
```

---

## 65. Indomitable Might's damage option and attached restrictions in the client listing

**Status:** open · **Found:** 2026-10-26, while finishing entry 51 (Aura/Equipment restrictions as the attachment's own abilities)

**Failing tests:**
- none yet (write one: Indomitable Might on a creature that loses all abilities to Dress Down, then blocked)

**What the rules require.** "Enchanted creature's controller may have it assign its combat damage as
though it weren't blocked" (Indomitable Might, AFC 40) is a static ability of the Aura, not an ability
granted to the creature (CR 113.10: only "has"/"gains" text adds an ability), so it keeps working after
the creature loses its abilities (CR 613.1f). The card wraps `AssignCombatDamageAsThoughUnblockedEffect`
in `GrantEffectEffect(..., ENCHANTED_CREATURE)`, and `CombatDamageService` only reads that effect from the
creature's own and granted effects. Separately, restrictions now modelled with
`AttachedPermanentRestrictionEffect` (Anti-Magic Aura, Artifact Ward, Spectral Shield, Consecrate Land,
Assault Suit, Airtight Alibi) are no longer granted effects, so `GrantedAbilityViewFactory` no longer
lists them on the enchanted/equipped permanent in the client.

**Why it's large.** Small engine change in `CombatDamageService` (read the effect from attached Auras,
honouring Volrath's Curse and the Aura losing its own abilities) plus a client-listing source for
attachment-imposed restrictions; both need tests.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 65. Make CombatDamageService read
AssignCombatDamageAsThoughUnblockedEffect from Auras attached to the attacker (like
GameQueryService.hasAuraWithEffect) and put Indomitable Might's effect directly in STATIC instead of a
GrantEffectEffect; add a Dress Down test. Then have the permanent view list restrictions that attached
Auras/Equipment impose through AttachedPermanentRestrictionEffect. Verify every CR number with the rules
MCP. Don't run the full test suite; ask me. Don't commit until I say so.
```

## 66. Activated-ability indices shift when abilities drop out of the effective list

**Status:** open · **Found:** 2026-10-26, while finishing entry 52 ("loses this ability" listing and copies)

**Failing tests:** none yet.

**What the rules require.** Each activated ability is its own ability; "activate only once each turn" and similar
limits track that ability. The engine keys per-turn and per-game activation counts
(`GameData.activatedAbilityUsesThisTurn`) by the position in `AbilityActivationService.getEffectiveActivatedAbilities`.
That list drops abilities the permanent lost ("loses this ability", losing all non-mana abilities), so a later
ability's position shifts mid-turn and it can inherit or lose another ability's count. Separately, `CardViewFactory`
leaves exile-only abilities out of the client's list while the engine list keeps them, so a client index after an
exile-only ability points one too far.

**Why it's large.** Activation limits, the AI and the client all use the position; keying counts by ability
identity touches every limit check.

**Prompt to start it:**

```
Fix missing-features-to-do.md entry 66: key activation-limit counts by ability rather than effective-list position,
and make the client ability list and the engine's effective list agree on exile-only abilities. Write tests first:
a permanent with a lost ability before a once-per-turn ability, activated before and after the loss.
Don't run the full test suite; ask me. Don't commit until I say so.
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
