# Missing features to do

Engine features that don't exist yet, found while fixing card tests. Each entry lists the tests that
fail because the feature is missing, what the rules require, and a ready-to-use prompt that kicks off
the implementation.

To add an entry, copy the template at the bottom, fill it in, and append it above the template.
Remove an entry once the feature has landed and its tests pass.

---

## 52. "Loses this ability": client listing and copy effects

**Status:** open · **Found:** 2026-10-23, after adding `LoseThisActivatedAbilityEffect` for Great Fang Chroniclers

**Failing tests:** none yet.

**What the rules require.** After "then this creature loses this ability" the permanent no longer has that
activated ability. The engine records it by printed-ability index in `Permanent.lostPrintedActivatedAbilityIndices`,
and activation and `PredicateEvaluationService.hasAnyPrintedAbility` honour it. Two gaps remain:
- `PermanentViewFactory` strips activated abilities only for `losesAllAbilities`/`losesAllNonManaAbilities`, so the
  client (and anything listing abilities from the view) still offers the lost ability until activation is refused.
- The record is an index into the card's printed abilities. If the permanent later becomes a copy of something else
  or changes face, the index would hide the new object's ability at that position. Only the removed ability itself
  should stay gone, so the record should be tied to the ability, or cleared or reinterpreted when the copiable
  values change. Not verified with a test yet.

**Why it's large.** Small, but it touches the view layer and copy and transform handling.

**Prompt to start it:**

```
Finish "loses this ability" (missing-features-to-do.md entry 52): hide lost printed activated abilities in
PermanentViewFactory, and make the lost-ability record survive copy and transform correctly. Write tests first:
Great Fang Chroniclers after using its ability, then becoming a copy of a creature with an activated ability.
Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 53. Ordering token-creation replacements outside the single-blueprint creation path

**Status:** open · **Found:** 2026-10-25, while finishing entry 35 (token replacement ordering)

**Failing tests:**
- None yet. Write one per path, e.g. Queen Allenal of Ruadach + Parallel Lives with a token copy
  (Cackling Counterpart) or a token created through `DestructionSupport.createTokenForPlayer`: the player
  must be asked, and Queen first must give the doubled Soldier.

**What the rules require.** CR 616.1 / 616.1e: the player creating the tokens chooses the order of every
replacement that modifies the token creation event. `PermanentControlSupport.applyCreateToken` (the
single-blueprint path) now runs a `TokenCreationEvent` where count replacements (`TokenCreationReplacements`)
and the Soldier/Frog/Squirrel additions (`TokenAdditionReplacements`) are ordered. Still fixed, with no prompt:
- `DestructionSupport.createTokenForPlayer`, `TokenCopySupport` and the copy handlers that call
  `getTokenCreationAmount` (multiplier first, additions unmultiplied).
- In the single-blueprint path, the Map, Thopter, Mutagen, Food (Tippy-Toe), Treasure (Bilbo), Academy
  Manufactor and solved-Case Clue additions/replacements are applied after the ordered event, so a doubler
  never doubles them and Chatterfang never counts them (before entry 35 Chatterfang counted them).

**Why it's large.** Each remaining site creates tokens mid-effect and would need to suspend on the order
prompt and resume (as `ChoiceHandlerService` does for `TokenCreationEvent`). Academy Manufactor replaces the
token kind, so it needs a non-count option on the event.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 53: route the remaining token creation paths
(DestructionSupport.createTokenForPlayer, TokenCopySupport, the token copy handlers) and the remaining
additions (Map, Thopter, Mutagen, Tippy-Toe Food, Bilbo Treasure, Academy Manufactor, solved Case Clues)
through TokenCreationEvent and ReplacementOrderService, as TokenAdditionReplacements does for Queen
Allenal, Quina and Chatterfang. Write the tests first. Don't run the full test suite; ask me. Don't
commit until I say so.
```

---

## 54. Nafs Asp's pay-before-draw special action has no client action, and the AI never pays

**Status:** open · **Found:** 2026-10-25, while finishing entry 36 (Quenchable Fire's special action)

**Failing tests:**
- None yet. Write one: a human player damaged by Nafs Asp (or Glass Asp) is offered the payment in
  `GameStateMessage.delayedTriggerPayments` while holding priority before their draw step, auto-pass stops
  for it, and paying clears `priorityPassedBy`.

**What the rules require.** CR 116.2c: paying "unless they pay {1} before that draw step" is a special action
the player may take any time they have priority until the step begins; CR 117.4: an earlier pass no longer
counts as passing in succession after an action. Entry 36 added the generic delayed-trigger payment
(`GameActionAvailabilityService.getDelayedTriggerPaymentsOwedBy`, `AbilityActivationService.payDelayedTriggerCost`,
`PAY_DELAYED_TRIGGER_COST`, a side-panel button and an auto-pass stop) for Quenchable Fire and Sabertooth
Cobra. Nafs Asp and Glass Asp (`LoseLifeAtNextDrawStepUnlessPays`) still pay only through
`GameService.payDrawStepLifeLoss`, which no client message reaches, auto-pass doesn't stop for, and which
keeps `priorityPassedBy` (`AbilityActivationServiceTest.drawStepPaymentRemovesOnlyOneIdenticalObligation`
asserts that). The AI never makes any of these payments.

**Why it's large.** Moving the Asps onto the generic payment changes their payment path and the service
tests that pin it; teaching the AI when paying is worth it needs a planner hook for special actions.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 54: add LoseLifeAtNextDrawStepUnlessPays (Nafs Asp, Glass Asp)
to GameActionAvailabilityService.getDelayedTriggerPaymentsOwedBy so it goes through
AbilityActivationService.payDelayedTriggerCost and the PAY_DELAYED_TRIGGER_COST client action, retire
payDrawStepLifeLoss, and let the AI pay delayed-trigger costs when it is worth it. Don't run the full test
suite; ask me. Don't commit until I say so.
```

---

## 55. Other "until end of turn" stores under Runed Terror's sequential phases

**Status:** open · **Found:** 2026-10-25, while finishing entry 38 (Runed Terror durations)

**Failing tests:**
- None yet. Write one per store moved, in the style of `RunedTerrorTest.temporaryBoostSurvivesOtherPlayersCleanup`
  (e.g. player 2 casts a "can't block this turn" or damage-prevention spell during their phase; it must still
  apply after player 1's cleanup step and end at player 2's).

**What the rules require.** Runed Terror ruling (2024-11-08): "until end of turn" and "this turn" effects end
during the cleanup step of the player who was active when the effect began (CR 514.2 ends them at cleanup).
Entry 38 made this hold for floating continuous effects (`floatingEffectCreatingActivePlayerIds`,
`GameData.expireEndOfTurnFloatingEffectsCreatedDuring`) and for `Permanent` power/toughness modifiers and
granted keywords (`GameData.phaseSequenceModifierShares`, `TemporaryModifierShare`). Every other store that
`TurnCleanupService.resetEndOfTurnModifiers` / `Permanent.resetModifiers` clears (the other per-permanent
flags such as `cantBlockThisTurn`, `removedKeywords`, protections, transient types/colors, base P/T
overrides, damage-prevention and redirection shields, "this turn" player sets and cast/play permissions)
still ends at whichever player's cleanup comes first. A keyword granted by the active player and again by
another player (same keyword on a `Permanent.grantedKeywords` set) is credited only to the first granter.

**Why it's large.** Each store needs the creating active player recorded, or a per-player split like
`TemporaryModifierShare`, and roughly two hundred stores are involved.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 55: extend entry 38's Runed Terror duration handling
(TurnCleanupService.attributeTemporaryModifiersToActivePlayer / phaseSequenceModifierShares and the
floating-effect tagging) to the remaining "until end of turn" / "this turn" stores, so each ends only at the
cleanup step of the player who was active when it began (Runed Terror ruling; CR 514.2). Start with the
most-used ones (Permanent.resetModifiers flags, damage-prevention shields) and add RunedTerrorTest cases for
each. Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 56. A standalone "any colour" mana activation still holds its non-mana triggers

**Status:** open · **Found:** 2026-10-25, while finishing entry 39 (mana-ability trigger timing)

**Failing tests:**
- None yet. Write one: activate Birds of Paradise (or a land whose mana needs a colour prompt) with no
  payment intent while a non-mana trigger watches it, answer the colour prompt, and expect the trigger on
  the stack.

**What the rules require.** CR 603.3: a triggered ability goes on the stack the next time a player would
receive priority. A mana ability activated with no payment in progress resolves, and its controller then
receives priority (CR 117.3c), so its non-mana triggers belong on the stack then. Since entry 39,
`GameService.finishPriorityManaActivation` does this for activations that finish at once. An activation that
stops for a colour choice parks in `GameData.pendingRevertableManaActivation`. The answer handlers then
complete it into a revertable `ManaActivation`, and `GameService.handleInteractionAnswer` still uses the
old flush, which waits while any revertable activation exists. So with no payment intent, its triggers
wait until the next priority pass or cast.

**Why it's large.** The payment intent is only known on the `tapPermanent`/`activateAbility` call. To
carry it across the colour prompt, `PendingManaActivation` (or a sibling field) has to record "standalone",
and every `completeParkedManaActivation` call site in `ChoiceHandlerService` (about ten) has to commit
instead of recording a revertable activation. Then the answer path needs the standalone flush.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 56: when a mana ability activated with no ManaPaymentIntent
parks for a colour choice (pendingRevertableManaActivation), make its non-mana triggers go on the stack once
the colour is chosen (CR 603.3, CR 117.3c), and make that activation non-revertable, matching what
GameService.finishPriorityManaActivation already does for activations that finish at once. Keep
intent-tagged (payment) activations held and revertable. Add tests next to ManaActivationRevertTest. Don't
run the full test suite; ask me. Don't commit until I say so.
```

---

## 57. Per-unit copy riders for Pyromancer's Goggles and Primal Wellspring

**Status:** open · **Found:** 2026-10-25, while finishing entry 40 (Sunken Palace's per-unit copy rider)

**Failing tests:**
- `PyromancersGogglesTest` "Each mana produced with a mana multiplier grants its own copy"
- `PyromancersGogglesTest` "Spending Goggles mana on a non-red spell cannot copy a later red spell"
- `PyromancersGogglesTest` "Only Goggles mana actually spent on a spell creates copies"

**What the rules require.** CR 106.6 / 106.6a: "When that mana is spent to cast a red instant or
sorcery spell, copy that spell" is a delayed trigger on the mana itself, one per unit produced. The
engine still arms these per player (`GameData.pendingNextRedInstantSorceryCopyCount`,
`pendingNextInstantSorceryCopyCount`, set in `AnyColorManaChoiceSupport`), so the next matching spell is
copied whatever mana paid for it. Sunken Palace now uses a pool tag instead
(`ManaPool.addCopyGrantingMana` / `takeSpentCopyGrantingMana`, claimed in
`TriggerCollectionService.checkSpellCastTriggers`, discarded at the end of every action by
`GameData.discardUnclaimedSpentManaRiders`). Goggles and Wellspring need the same tag, with the spell
filter (red instant/sorcery, instant/sorcery) checked when the spent units are claimed: units spent on a
spell that doesn't match are spent without effect. Also: Sunken Palace's mana that is added only after a
prompt (a land-mana replacement order or color choice) is not tagged yet (`tagCopyGrantingMana` in
`ActivatedAbilityExecutionService` runs only when no mana choice is pending).

**Why it's large.** Medium: two more tags through every `ManaPool` copy/merge/convert/drain path, and the
any-color choice handlers must tag the chosen color.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 57: replace the per-player pendingNextRedInstantSorceryCopyCount /
pendingNextInstantSorceryCopyCount arming for Pyromancer's Goggles and Primal Wellspring with per-unit
ManaPool tags like Sunken Palace's copyGrantingMana (CR 106.6a), so only the matching spell the tagged
mana actually pays for is copied, once per unit. Make the failing PyromancersGogglesTest cases pass and
add Primal Wellspring tests. Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 58. Opponent-chosen-target spells cast from another player's hand (Sen Triplets)

**Status:** open · **Found:** 2026-10-25, while finishing entry 41 (casting from another player's hand)

**Failing tests:** none yet. Casting such a spell from another player's hand is refused with an
`IllegalStateException`.

**What the rules require.** Sen Triplets' rulings say a card played from that player's hand is played
"just like playing any other card", so a spell whose targets an opponent chooses (a
`GainControlOfTargetEffect` with `opponentChoosesTarget`, or `Card.getOpponentChosenSpellTargetIndices`)
can be cast from it. The engine stops the cast for the opponent's choice
(`PermanentChoiceContext.OpponentChosenSpellTarget(s)`) and resumes it with
`SpellCastingService.playCardAfterOpponentChosenTarget(s)`, which indexes the caster's own hand, so it
would cast the wrong card. `SpellCastingService.rejectOpponentChosenTargetsFromAnotherHand` refuses the
cast instead. Also open: the hard AI's MCTS search (`GameSimulator`) doesn't list plays from another
player's hand; only the base `AiDecisionEngine.tryPlayFromOpponentHand` heuristic plays them.

**Why it's large.** Small to medium: the two prompt contexts need the hand owner, and their resume path
has to run inside `SpellCastingService.playFromHandOf` so the owner gets stamped.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 58: let a spell whose targets an opponent chooses be cast from
another player's hand under Sen Triplets. Carry the hand owner in PermanentChoiceContext.OpponentChosenSpellTarget
and OpponentChosenSpellTargets, resume the cast through SpellCastingService.playFromHandOf, and remove
rejectOpponentChosenTargetsFromAnotherHand. Optionally add foreign-hand plays to GameSimulator's action list.
Add SenTripletsTest coverage. Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 59. Split-card names in "choose a card name" prompts and chosen-name checks

**Status:** open · **Found:** 2026-10-25, while finishing entry 42 (aftermath cards as split cards)

**Failing tests:** none yet.

**What the rules require.** "Each split card has two names. If an effect instructs a player to choose a
card name and the player wants to choose a split card's name, the player must choose one of those names
and not both. An object has the chosen name if one of its names is the chosen name" (CR 709.4a).
`PlayerInputService.collectCardNamesInGameExcluding` offers each split card's whole name ("Commit //
Memory") as well as its halves, so a player can choose both names at once. Several chosen-name checks
still compare `getName()` to the chosen name instead of using `Card.hasName`, so they miss a split card
(or a Room) named by one half: `GameQueryService` (player protection from the chosen name, damage from
a source with the chosen name, activated abilities of the chosen name),
`AbilityActivationService.validateNotBlockedByNameLock`, `CastingPermissionService.isLandPlayForbiddenByChosenName`,
`IncreaseCastCostForChosenNameSpellsEffectHandler` and `TargetLegalityService`'s
`StackEntrySharesChosenNameWithSourcePredicate`. Entry 42 switched the card-in-zone checks (Conundrum
Sphinx, Mindblaze, Liar's Pendulum, Booby Trap, `CardHasSourceChosenNamePredicate`) to `hasName`.

**Why it's large.** Small to medium. The string-based checks take a name, not a card, so their callers
need to pass the object; and dropping the whole name from the prompt may break tests that choose it.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 59 (CR 709.4a): stop offering a split card's whole name in
"choose a card name" prompts (PlayerInputService.collectCardNamesInGameExcluding and the catalog name
list), and make the remaining chosen-name checks listed in the entry match either half's name through
Card.hasName. Add card tests with an aftermath or split card (e.g. Commit // Memory, Fire // Ice) for
each touched effect. Verify every CR number with the rules MCP. Don't run the full test suite; ask me.
Don't commit until I say so.
```

---

## 60. Stack-static abilities of a copied instant or sorcery (Torrent of Lava copies)

**Status:** open · **Found:** 2026-10-25, while finishing entry 43 (stack-static abilities)

**Failing tests:**
- none yet (a copy of Torrent of Lava grants no ability; untested)

**What the rules require.** CR 113.6: abilities of an instant or sorcery spell function while it is
on the stack, and a copy of a spell copies its abilities (CR 707.2). A copy of Torrent of Lava
should make each creature have "{T}: Prevent the next 1 damage that [the copy] would deal to this
creature this turn" as long as the copy is on the stack. `LayerSystemService.stackStaticSources`
leaves copies out, because the card bakes the original card's id into
`PreventNextDamageToSourceFromCardEffect` and `CopySupport.createCopyCard` shares the effect list,
so a copy's grant would shield against the original spell instead of the copy.

**Why it's large.** The prevention effect needs to refer to "this spell" by the stack object that
grants it (e.g. resolve the source from the granting stack entry at activation) instead of a card
id fixed at construction, and the copy path needs a test with a spell-copy effect.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 60: let a copy of an instant or sorcery on the stack grant
its WrappedStackStaticEffect abilities (CR 113.6, CR 707.2), with Torrent of Lava's granted
prevention keyed to the copy as the damage source. Stop excluding copies in
LayerSystemService.stackStaticSources once the source reference is per stack object. Verify every
CR number with the rules MCP. Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 61. Four vote handlers never report their votes as finished

**Status:** open · **Found:** 2026-10-25, while finishing entry 45 (optional additional votes)

**Failing tests:** none yet; a Model of Unity or Erestor of the Council test with one of these votes would fail.

**What the rules require.** "Whenever players finish voting" abilities (Model of Unity, Erestor of the
Council) trigger after every vote (CR 701.38a defines voting). `FatefulTempestEffectHandler`,
`SelvalasStampedeEffectHandler`, `WillOfThePlaneswalkersEffectHandler` and
`VoteForInnocentOrGuiltyEffectHandler` never call `VotingFinishedSupport.finishVoting`, and
`ChoiceHandlerService.recordVotingChoiceIfApplicable` doesn't record their votes, so those triggers
never see these votes.

**Why it's large.** Small: record the votes (all four contexts already implement `VoteChoiceContext`)
and call `finishVoting` where each vote ends, then add a Model of Unity test per card.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 61: make Fateful Tempest, Selvala's Stampede, Will of the
Planeswalkers (Path of the Ghosthunter / Path of the Animist) and the innocent-or-guilty vote record
their votes and call VotingFinishedSupport.finishVoting, so "whenever players finish voting" triggers
(Model of Unity, Erestor of the Council) fire for them. Add tests. Verify every CR number with the
rules MCP. Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 62. Krark's Thumb result choice for interleaved, nested and non-resolution coin flips

**Status:** open · **Found:** 2026-10-25, while finishing entry 46 (Krark's Thumb result choice)

**Failing tests:** none yet; a Krark's Thumb test with mixed mocked flips on any path below would
show the heads result kept without a prompt.

**What the rules require.** Krark's Thumb: "If you would flip a coin, instead flip two coins and
ignore one." Its controller sees the results and chooses which to ignore (a replacement effect,
CR 614.1a; CR 705.3 covers effects that fix a flip's result). Only the first coin-flip event of a
handler marked `ChoosesCoinFlipResultsFirst` asks: `CoinFlipService` stops the handler with
`CoinFlipChoicePending` and `EffectResolutionService` resolves it again. Everywhere else the engine
still keeps heads when the physical flips disagree:
- handlers that change the game between flips: `FlipUntilLoseEffectHandler`,
  `FlipUntilLosePutCountersOnSourceEffectHandler`, `FlipCoinsUntilLoseEffectHandler`,
  `FlipUntilLoseOrStopSupport` (Fiery Gambit), `ManaClashEffectHandler`,
  `EachPlayerFlipsCoinAndTailsSacrificesCreatureEffectHandler`, `FlipCoinForEachOpponentEffectHandler`,
  `FlipCoinForEachBlockingCreatureEffectHandler`, `RiskyMoveEffectHandler`, `GameOfChaosSupport`,
  `AmuletOfQuozAnteEffectHandler`;
- a marked handler's later flips, and flips of effects it dispatches (a branch that flips again);
- flips outside stack resolution: `CoinFlipCostSupport` (costs), `FlipCoinOnEnterEffectHandler`,
  `PermanentChoiceBattlefieldHandlerService`, and `D20RollService`'s coin flips.

**Why it's large.** Loop handlers need their flips journaled across reruns (log and trigger side
effects between flips must not repeat), or a resumable state machine per handler. Cost and entry
paths need their own pause points outside the parked-resolution mechanism.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 62: extend the Krark's Thumb result choice
(ChoiceContext.CoinFlipResultChoice, CoinFlipService.recordedCoinFlips, CoinFlipChoicePending) to
the coin-flip paths listed in the entry: flip-until-lose loops, Mana Clash, per-player and per-blocker
flips, Risky Move, Game of Chaos, Amulet of Quoz, nested flips, coin-flip costs, entry flips and
D20RollService. Keep replayed flips from repeating logs or triggers. Add Krark's Thumb tests with
mocked mixed flips for each. Verify every CR number with the rules MCP. Don't run the full test
suite; ask me. Don't commit until I say so.
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
