# Missing features to do

Engine features that don't exist yet, found while fixing card tests. Each entry lists the tests that
fail because the feature is missing, what the rules require, and a ready-to-use prompt that kicks off
the implementation.

To add an entry, copy the template at the bottom, fill it in, and append it above the template.
Remove an entry once the feature has landed and its tests pass.

---

## 55. Other "until end of turn" stores under Runed Terror's sequential phases

**Status:** partial · **Found:** 2026-10-25, while finishing entry 38 (Runed Terror durations)

**Progress (2026-10-27):** Generalized the per-player split. `TemporaryModifierShare` became
`EndOfTurnStateShare` (one value per `EndOfTurnStateField`, split/merged/capped by the field's `Kind`), stored in
`GameData.phaseSequenceEndOfTurnShares` plus `phaseSequenceAttributedEndOfTurnState`, and credited by
`TurnCleanupService.attributeEndOfTurnStateToActivePlayer`. It now covers every `Permanent.resetModifiers` store
except the combat-only ones (`mustAttackThisCombat`, `animatedUntilEndOfCombat`, `mustBlockIdsUntilEndOfCombat`) and
the `creaturesThatCrewedThisTurn` tracker, the simple per-permanent prevention/destruction/regeneration shields,
`GameData.playerDamagePreventionShields` / `playerCombatDamagePreventionShields`, and (second pass) the game-wide
`GAME_` fields: `globalDamagePreventionShield`, the `preventAll*` flags (with `damageByCreaturesPreventionLifeGainPlayers`
and `combatDamageExemptPredicatesByController`), `preventDamageFromColors`, the colour / colorless prevention maps,
the `playersWith*Prevented` / `creatures*Prevented` / `creaturesAssigningNoCombatDamageThisTurn` sets and
predicate maps, `playersRedirectingAllCreatureDamage`, `damageCantBePreventedThisTurn`, the global
`playersCantGainLife` / `playersCantLoseLife` / `playersCantSearchLibraries` / `creaturesCantAttack` "this turn"
flags and the `playersWhoCantGainLife` / `GainNoLife` / `CantLoseLife` sets (`GameData.captureGameEndOfTurnState`,
tracked under `GameData.GAME_END_OF_TURN_STATE_ID`; a map of sets is a `SET` of `Map.entry` pairs). A third pass added
the player-scoped "this turn" protections, restrictions, permissions and replacements: `playersWithHexproof` /
`Shroud`, `playerHexproofFromColors`, `permanentHexproofFromColors`, `playerProtectionFromColors` / `FromPlayerIds`,
`playerKeywordsUntilEndOfTurn`, the can't-be-countered and creatures-can't-be-targeted-by-colour stores,
`playersSilenced`, can't-play-lands / from-graveyards, `playersCantCastSpellTypes` / `Noncreature`, the three
can't-activate sets, `playerCreaturesCantAttackThisTurn` (a map of lists, a `LIST` of `Map.entry` pairs), Gather
Specimens / Crafty Cutpurse lists, the uncast-entering / shuffle-from-exile / exile-instead-of-dying /
exile-instead-of-graveyard / bottom-of-library sets, `playersWithFlash` / `FreeHandCast`, look-at-face-down,
play-face-up-from-exile, `cardTypeFlashGrantsThisTurn` and `playersAllowedTwiceLoyaltyActivations`. A shield used up
in between is not restored (capped by what remains). RunedTerrorTest covers can't-block, creature and player
prevention shields, a used-up shield, Fog, Safe Passage, Eerie Interference, Skullcrack, Silence, Gilded Light and
Borne Upon a Wind. **Still open:** the
paired-list regeneration shields (`opponentDrawRegenerationShield*`, `plusOnePlusOneCounterRegenerationShield*`,
`gainControlRegenerationShields`), `tappedPermanentsForAbilityThisTurn`,
`temporaryGraveyardCardAnimationsUntilEndOfTurn`, and the remaining `GameData` stores cleared in
`resetEndOfTurnModifiers`: one-shot redirect/prevention shield lists and maps (`damageRedirectShields`,
`sourceDamageRedirectShields`, `playerSourceNextDamageShields`, `targetSourceDamagePreventionShields`, Eye for an
Eye, `playerColorDamagePreventionCount`, `playerSourceDamagePreventionIds` with its paired colour/life-gain maps,
`playerNextDamageFromMatchingSourcesPrevented`, `combatDamagePreventionTokenShields`, `channelHarmShields`, ...),
`playerStaticEffectsUntilEndOfTurn`, enter-tapped / extra-counter "this turn" stores, block requirements, damage
bonuses / doublings / life floors / replacements, the remaining "this turn" rule stores (`playersMaxSpellsThisTurn`,
`playerExtraLoyaltyActivationsThisTurn`, `spellColorOverridesUntilEndOfTurn`, Sen Triplets' controller ids,
next-spell flash / convoke / cost / free-cast grants, next-spell copy counters, mana-fixing and mana replacement
stores), and graveyard / exile / library-top cast permissions. Count-valued maps
(`Map<UUID, Integer>`) and one-shot shield lists need a per-key `SHIELD`-style split rather than the entry-set
`SET` used so far. Known limitation: a flag or set element (including a keyword) that another player's
effect sets again while it is already set stays credited to the first player only, and with three or more players a
single-value field kept for several other players takes the value of the most recently credited player.

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
