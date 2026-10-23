# Missing features to do

Engine features that don't exist yet, found while fixing card tests. Each entry lists the tests that
fail because the feature is missing, what the rules require, and a ready-to-use prompt that kicks off
the implementation.

To add an entry, copy the template at the bottom, fill it in, and append it above the template.
Remove an entry once the feature has landed and its tests pass.

---

## 34. Asking death replacements before the creature leaves, outside state-based actions

**Status:** open (partly done) · **Found:** 2026-10-18, while implementing CR 616.1 death replacement ordering

**Failing tests:**
- None yet. Write one per remaining gap, e.g. Kalitas, Traitor of Ghet on one side, Gravebane Zombie
  on the other and Diabolic Edict while the Zombie's controller also has another creature (the sacrifice
  happens in the choice completion), or the Zombie sacrificed as a cost with Rest in Peace out.

**What landed (2026-10-23).** A resolving effect whose handler implements `AsksDeathReplacementsFirst`
(changes nothing before its first removal attempt) is stopped before anything happens when a creature
it would kill has competing death replacements and one keeps the card. The controller answers a
`DeathExileReplacementChoice` with `rerunsEffect`, the effect is parked and resolved again
(`rerunCurrentEffectAfterInteraction`), and the rerun's deaths consume the answers from
`GameData.chosenDeathReplacementsForRerun`. `DestructionSupport` batches ask every member before any
is destroyed. Marked so far: `DestroyTargetPermanentEffectHandler`, `DestroyTargetPermanentThenEffectHandler`
and `DestroyAllPermanentsEffectHandler`. Tests: `GravebaneZombieTest` (Grisly Spectacle's mill sees the
Zombie on top of the library; Wrath of God asks before any creature is destroyed) and
`KalitasTraitorOfGhetTest.murderAsksForTheDeathReplacementBeforeTheZombieLeaves`.

**Also landed (2026-10-23, second pass).** Reviewed and marked `SacrificePermanentsEffectHandler` (its
"has no matching permanents" log lines for multi-player sacrifices now come after the sacrifice, so
nothing changes first), `SacrificeEachMatchingPermanentEffectHandler`, `SacrificeTargetCreatureEffectHandler`,
`DestroyUpToOneTargetPermanentEffectHandler` and `DestroyTargetPermanentsThenEffectHandler`.
`DestructionSupport.performSimultaneousSacrifice` and `SacrificeEachMatchingPermanentEffectHandler` ask the
whole batch before anything is sacrificed. Tests: `KalitasTraitorOfGhetTest.edictAsksForTheDeathReplacementBeforeTheZombieIsSacrificed`
(Diabolic Edict on a lone Zombie), `GravebaneZombieTest.eachPlayerSacrificeAsksBeforeAnyCreatureIsSacrificed`
(Innocent Blood: Kalitas is still on the battlefield while asked) and
`GravebaneZombieTest.sacrificeEachMatchingAsksBeforeAnyPermanentIsSacrificed` (All Is Dust).

**Also landed (2026-10-23, third pass).** Reviewed and marked 46 more destroy handlers whose first change
is the destruction (recording last-known info on the stack entry doesn't count), among them every
single-target and batch destroy with a rider afterwards (`DestroyTargetCreatureThenCreateTokenEqualToPowerToughnessEffectHandler`,
`DestroyAllCreaturesSharingSacrificedCreatureTypeEffectHandler`, `DestroyReferencedPermanentEffectHandler`,
`DestroySourceEffectHandler`, `DestroyCombatOpponentEffectHandler`, `DismantleEffectHandler`, `SeasonOfTheWitchEffectHandler`,
`WaveOfVitriolEffectHandler`, ...; `grep -rl AsksDeathReplacementsFirst` lists them). Activated and triggered
abilities leave the stack before they resolve, so while asked they sit only in `GameData.pendingEffectResolutionEntry`.
Tests: `KalitasBloodchiefOfGhetTest.asksForTheDeathReplacementBeforeTheZombieIsDestroyed` (an activated
ability; the library answer means no Vampire) and `EndemicPlagueTest.asksForTheDeathReplacementBeforeAnyCreatureIsDestroyed`.
Destroy handlers reviewed and deliberately left unmarked:
- They clear a chosen value before destroying, so a rerun would lose or re-ask it: `DestroyAllCreaturesOfChosenParityEffectHandler`
  and `DestroyOtherCreaturesOfChosenManaValueParityEffectHandler` (`chosenSpellManaValueParity`),
  `DestroyAllCreaturesOfChosenTypeEffectHandler`, `DestroyAllCreaturesExceptChosenTypeEffectHandler` and
  `DestroyAllCreaturesExceptChosenTypesEffectHandler` (`chosenSpellSubtype`). Moving the clear after the
  destruction would make them safe, but check that nothing reads the value during the death choice.
- They pick at random before destroying, so a rerun would pick again: `DestroyRandomAttackingCreatureEffectHandler`,
  `DestroyOneOfTargetsAtRandomEffectHandler`, `DestroyRandomOpponentPermanentWithCounterEffectHandler`,
  `FlipCoinForEachMatchingPermanentDestroyOnLossEffectHandler`, `ChooseNonlandPermanentForEachOpponentThenDestroyOneAtRandomEffectHandler`.
- They deal damage or counter something first: `DealDamageToAnyTargetThenDestroyIfDamagedEffectHandler`,
  `DealDamageToTargetCreatureThenDestroyIfDamagedEffectHandler`, `CounterSpellOrAbilityAndDestroySourceEffectHandler`.
- They only start a choice or payment and destroy in its completion (`DestroyTargetPermanentUnlessControllerTakesDamageEffectHandler`,
  `DestroyUpToOneAttachedPermanentEffectHandler`, the "each player chooses ... then destroy" handlers): the
  completion gap below.

**Also landed (2026-10-23, fourth pass).** Reviewed and marked 46 sacrifice handlers whose first change
is the sacrifice (or that only start a choice), among them every edict whose lone candidate is sacrificed
in the handler (`TargetPlayerSacrificesCreatureThenDrawsPowerEffectHandler`, `SacrificeCreatureAndControllerGainsLifeEqualToToughnessEffectHandler`,
`SacrificePermanentOrElseEffectHandler`, the "sacrifice one of two targets" family, ...) and the each-player
batches that end in `performSimultaneousSacrifice` (`EachPlayerSacrificesCreatureEffectHandler`, the "choose
... then sacrifice the rest" handlers, `EachPlayerSacrificesDownToCount/Fewest`, the fraction handlers, ...).
Handlers that sacrifice several permanents one by one in one event (`SacrificeAttackingCreaturesEffectHandler`,
`DamageSourceControllerSacrificesPermanentsEffectHandler`, `SacrificePermanentsToUntapSelfEffectHandler`) now
ask the whole group first through `DestructionSupport.askDeathReplacementsBeforeSacrifice`. Tests:
`GravebaneZombieTest.sacrificeThenDrawAsksBeforeTheZombieIsSacrificed` (Twisted Justice: no card drawn while
asked, three after) and `GravebaneZombieTest.sacrificeThenGainLifeAsksBeforeTheZombieIsSacrificed` (Devour Flesh).
Sacrifice handlers reviewed and deliberately left unmarked, since they change the game first:
`EachPlayerFlipsCoinAndTailsSacrificesCreatureEffectHandler` (coin flips), `LivingDeathEffectHandler` (exiles
graveyards), `EachPlayerSacrificesPermanentOrDiscardsEffectHandler` (sets `gameData.plaguecrafter` state),
`EachPlayerChoosesCreatureThenSacrificesRestEffectHandler` (places counters and floating effects). Still
unreviewed: the remaining larger sacrifice handlers (list them with the grep below; `AnyOpponentMay...`,
`...UnlessDiscard/PaysLife/LosesLife`, `ForcedCostOrElse`, `TormentOfHailfire`, `KillingWave`, `InfernalOffering`, ...).

**Also landed (2026-10-23, fifth pass).** Reviewed and marked the 32 `Sacrifice*EffectHandler`s that sacrifice
one permanent (themselves, an enchanted or equipped creature, a target, the triggering permanent, an
"unless" source) or delegate to a marked handler (`SacrificeSelf...`, `SacrificeEnchanted...`, `SacrificeTarget...`,
`SacrificeTriggering...`, `SacrificeUnless...`, `SacrificeGranting...`, `SacrificePermanentsThen`, `SacrificePermanentsOrLoseGame`,
`SacrificeOtherCreaturesThenReturnCreatureCards`, ...). `SacrificeEnchantedCreatureAndCreateToken`, `SacrificeEnchantedCreatureOnLeave`
and `SacrificeTriggeringPermanent` now log the sacrifice after it rather than before, and `SacrificePermanentsThen`
asks its whole group before the first sacrifice. Tests: `GravebaneZombieTest.sacrificeTargetThenCreateTokensAsksBeforeTheZombieIsSacrificed`
(Mercy Killing: no Elf Warriors while asked, three after) and `GravebaneZombieTest.enchantedCreatureSacrificeAsksBeforeTheZombieIsSacrificed`
(Parasitic Implant: one Myr and one sacrifice log line). `SacrificeCreatureThenMassDamageEqualToPowerEffectHandler` only
starts a choice and sacrifices in its completion (the completion gap below). Every `Sacrifice*EffectHandler`
under `normalfx/` is now reviewed; the unreviewed ones are the `EachPlayer/EachOpponent/AnyOpponent/TargetPlayer...`
mixed handlers in the grep below.

**Also landed (2026-10-23, sixth pass).** Single-permanent sacrifice choice completions now get the window too.
`PermanentChoiceHandlerService.completeAskingDeathReplacementsFirst` opens `deathReplacementsCanRerunEffect` around
the completions that change nothing before the sacrifice (`PermanentChoiceContext.SacrificeCreature`, used by every
edict through `DestructionSupport.performSacrificeCreatureForPlayer`, plus `SacrificeCreatureOpponentsLoseLife`,
`SacrificeCreatureControllerGainsLifeEqualToToughness`, `SacrificeCreatureThenSearchLibrary` and the five
`TargetPlayerSacrifices...` contexts whose helpers the marked handlers share). A stop stores the answered prompt in
`GameData.pendingDeathReplacementChoiceReplay`; once the last rerun answer is in, `ChoiceHandlerService` completes the
same answer again (`completeChoiceAgainAfterDeathReplacements`) and the parked effect is left alone
(`resolveDeathExileReplacementChoice` no longer clears `rerunCurrentEffectAfterInteraction` for it). The
`InputCompletionService` epilogues close the window, so nothing after the completion's own sacrifice can stop it.
Tests: `KalitasTraitorOfGhetTest.edictChoiceAsksForTheDeathReplacementBeforeTheChosenZombieIsSacrificed` (Diabolic
Edict, Zombie chosen over Grizzly Bears: no sacrifice log line while asked, one after) and
`GravebaneZombieTest.chosenSacrificeThenGainLifeAsksBeforeTheZombieIsSacrificed` (Devour Flesh: no life gained while
asked). Still open among completions: multi-choices (`MultiPermanentChoiceContext.ForcedSacrifice`, "sacrifice, then"
multi-selects), the "each player chooses ... then destroy" completions (`EachPlayerChoosesCreatureToDestroy`,
`DestroyChosenCreature`, ...), the remaining single-sacrifice contexts (`SacrificeOneOfTwo*`, `ForcedCostOrElse`,
`LandEquilibriumSacrifice`, `TargetPlayerSacrificesNontokenCreatureThenConjuresDuplicate`, ...; each needs the same
"nothing before the sacrifice" review before it joins `sacrificesBeforeChangingAnything`), and sacrifices paid as costs.

**Also landed (2026-10-23, seventh pass).** The "choose ... then destroy" single-choice completions joined
the window (`PermanentChoiceHandlerService.killsBeforeChangingAnything`, renamed from `sacrificesBeforeChangingAnything`):
`DestroyChosenCreature` in its destroy mode (not its exile or villainous-shuffle modes) and the
`EachOpponentChoosesGreatestPowerCreatureToDestroy`, `EachOpponentChoosesPermanentToDestroy` and
`EachPlayerChoosesCreatureToDestroy` contexts, whose accumulated picks live in the immutable context, so the
replayed last answer rebuilds the same batch. Their starting handlers are now marked too (each only logs "no
creatures" on a path that destroys nothing): `TargetPlayerChoosesCreatureDestroyEffectHandler`,
`OpponentChoosesCreatureToDestroyEffectHandler`, `EachOpponentChoosesGreatestPowerCreatureToDestroyEffectHandler`,
`EachOpponentChoosesPermanentToDestroyEffectHandler` and `EachPlayerChoosesCreatureToDestroyEffectHandler`. Tests in
`KalitasTraitorOfGhetTest`: `targetPlayerChoosesDestroyAsksBeforeTheLoneZombieIsDestroyed` (Imperial Edict, lone
Zombie), `chosenDestroyAsksBeforeTheChosenZombieIsDestroyed` (Imperial Edict, Zombie chosen over Grizzly Bears: no
destroy log line while asked, one after) and `eachOpponentChoosesDestroyAsksBeforeTheChosenZombieIsDestroyed`
(Highcliff Felidar, Zombie chosen over a 3-power Bears). Still unreviewed among destroy completions:
`ChooseNonlandPermanentForEachOpponentThenDestroyOneAtRandom` (random pick, so it stays out) and the
`DestroyTargetPermanentUnlessControllerTakesDamage` / `DestroyUpToOneAttachedPermanent` completions.

**Also landed (2026-10-23, eighth pass).** Seven more single-choice sacrifice completions joined
`PermanentChoiceHandlerService.killsBeforeChangingAnything`, each reviewed for "nothing before the sacrifice":
`SacrificeOneOfTwo` (Incriminate), `SacrificeOneOfTwoThenCounterOnOther` (Retribution),
`SacrificeOneOfTwoThenReturnOtherToHand` (Barrin's Spite), `TargetPlayerSacrificesNontokenCreatureThenConjuresDuplicate`
(Grave Choice), `SacrificeCreatureCreateTokensIfSubtype` (Warren Weirding), `OpponentChoosesPermanentToSacrifice`
and `EachTargetPlayerLosesLifeAndSacrificesCreature` (its accumulated picks live in the immutable context, and any
life loss before the sacrifice already happened in the starting handler). Their starting handlers
`TargetPlayerSacrificesNontokenCreatureThenConjuresDuplicateEffectHandler` and
`TargetPlayerSacrificesCreatureThenCreateTokensIfSubtypeEffectHandler` are now marked too.
`EachTargetPlayerLosesLifeAndSacrificesCreatureEffectHandler` stays unmarked, since without `sacrificeBeforeLifeLoss`
its targets lose life first. Tests in `GravebaneZombieTest`: `sacrificeOneOfTwoThenReturnOtherAsksBeforeTheZombieIsSacrificed`
(the other creature stays until answered, then returns once), `sacrificeOneOfTwoThenCounterAsksBeforeTheZombieIsSacrificed`
(no -1/-1 counter while asked, one after), `targetPlayerSacrificeIfSubtypeAsksBeforeTheChosenZombieIsSacrificed`
and `targetPlayerSacrificeIfSubtypeAsksBeforeTheLoneZombieIsSacrificed`. Still unreviewed single-sacrifice contexts:
`LandEquilibriumSacrifice` (runs inside a land-entering replacement, not an effect), `TormentSacrifice` (shared
`GameData.torment` state), `ForcedCostOrElse`, the `AnyOpponent...`/`MaySacrifice...` "unless" contexts and the
"sacrifice another creature, then ..." contexts (`SacrificeAnotherCreature*`, `SacrificeCreatureCreate*`,
`SacrificeArtifactForDividedDamage`, `ExploitSacrifice`, `SacrificePermanentAnd*`, ...).

**Also landed (2026-10-23, ninth pass).** The "sacrifice another creature/permanent, then ..." single-choice
completions joined `PermanentChoiceHandlerService.killsBeforeChangingAnything`; each was reviewed to only read
(power, toughness, mana value, the source permanent, last-known info copied onto the parked entry) before its
sacrifice: `SacrificeAnotherCreatureGainLifeAndDraw` (Disciple of Bolas), `SacrificeAnotherCreatureDealPowerDamage`
(Heart-Piercer Manticore and friends: the reflexive target prompt opens only after the answer),
`SacrificeAnotherCreatureDrawAndMayPutPermanent`, `SacrificeArtifactForDividedDamage`, `ExploitSacrifice`,
`SacrificePermanentAndReturnTargetCards`, `SacrificePermanentAndBoostSelf`, `SacrificePermanentAndGrantKeywordSelf`,
`SacrificeCreatureCreateTokensEqualToToughness` and `SacrificeCreatureCreateSizedTokenEqualToPower`. Tests in
`GravebaneZombieTest`: `sacrificeAnotherCreatureThenGainLifeAndDrawAsksBeforeTheZombieIsSacrificed` (no life or
cards while asked, three of each after) and `sacrificeAnotherCreatureThenReflexiveDamageAsksBeforeTheZombieIsSacrificed`.
Still unreviewed single-sacrifice contexts: `LandEquilibriumSacrifice`, `TormentSacrifice`, `ForcedCostOrElse`,
the `AnyOpponent...`/`AnyPlayerMay...`/`MaySacrifice...` "unless" contexts, `SacrificePermanentThen`,
`SacrificeOtherCreatureThenRevealUntilLowerManaValue`, the `SacrificeOneOfCombatDamageDealers...` contexts and
`SacrificeCreatureThenMassDamageEqualToPower`.

**Also landed (2026-10-23, tenth pass).** The remaining "sacrifice ..., then" single-choice completions joined
`PermanentChoiceHandlerService.killsBeforeChangingAnything`; each only reads (power, mana value, a snapshot or
last-known info written onto the parked entry, which the replay overwrites with the same values) before its
sacrifice: `SacrificePermanentThen` (every `SacrificePermanentThenEffect` choice, the "may pay and sacrifice" and
counter-unless-sacrifice prompts; the reflexive trigger or follow-up effect comes after the sacrifice),
`SacrificeOtherCreatureThenRevealUntilLowerManaValue` (Kethek, Crucible Goliath),
`SacrificeOneOfCombatDamageDealersThenRevealMatchingCreature`, `SacrificeOneOfCombatDamageDealersThenRevealUntilSharedCreatureType`
(Descendants' Fury) and `SacrificeCreatureThenMassDamageEqualToPower` (Rupture; its starting handler only opens the
choice, so it stays unmarked). Tests in `GravebaneZombieTest`: `sacrificePermanentThenReflexiveAsksBeforeTheZombieIsSacrificed`
(Tip the Scales: no reflexive trigger while asked, one after), `sacrificeThenMassDamageAsksBeforeTheZombieIsSacrificed`
(Rupture: no life lost while asked, 3 each after), `sacrificeOtherCreatureThenRevealAsksBeforeTheZombieIsSacrificed`
(Kethek) and `sacrificeCombatDamageDealerThenRevealAsksBeforeTheZombieIsSacrificed` (Descendants' Fury). Still
unreviewed single-sacrifice contexts: `LandEquilibriumSacrifice`, `TormentSacrifice`, `ForcedCostOrElse` and the
`AnyOpponent...`/`AnyPlayerMay...`/`MaySacrifice...` "unless" contexts.

**Also landed (2026-10-23, eleventh pass).** The "any opponent / any player may sacrifice" and "you may sacrifice
for a counter" single-choice completions joined `PermanentChoiceHandlerService.killsBeforeChangingAnything`; each
sacrifices first and only then taps, grows, transforms, counters or prompts the next player (or, for the contexts
that collect picks in their immutable effect, only prompts the next player or ends in
`performSimultaneousSacrifice`, which asks the whole batch first): `MaySacrificeForCounterOnSource` (Ravenous Vampire,
Crovax, Chitterspitter, ...), `GargantuanGorillaSacrificeForest`, `AnyOpponentSacrificeCreatureForTapAndCounter`
(Desecration Demon), `AnyOpponentSacrificeNontokenCreatureForTapAndSeekLand`, `AnyOpponentSacrificeCreatureForTransform`,
`AnyOpponentSacrificeCreatureForTapAndGainLifeAndDraw`, `AnyPlayerMaySacrificeLandPutSourceOnTop` and
`AnyPlayerMaySacrificeCreatureToCounterSpell` (Brain Gorgers). Tests in `GravebaneZombieTest`:
`anyOpponentSacrificeAsksBeforeTheChosenZombieIsSacrificed` (Desecration Demon: untapped with no counter while asked,
tapped with one after), `maySacrificeForCounterAsksBeforeTheZombieIsSacrificed` (Ravenous Vampire) and
`anyPlayerSacrificeToCounterAsksBeforeTheChosenZombieIsSacrificed` (Brain Gorgers: not countered while asked).
Not covered: the same sacrifices made straight from the may prompt when the player controls only one candidate
(the mayfx handlers sacrifice on "yes" without a permanent choice, the same gap as Pulmonic Sliver's lone "may"),
and the collected-picks batches (Innocent Traveler, Brain Gorgers) when the last player declines, since their
`performSimultaneousSacrifice` then runs in the may completion. Still unreviewed single-sacrifice contexts:
`LandEquilibriumSacrifice`, `TormentSacrifice` and `ForcedCostOrElse`.

**Also landed (2026-10-23, twelfth pass).** Two more single-choice completions joined
`PermanentChoiceHandlerService.killsBeforeChangingAnything`. `ForcedCostOrElse` joins only when its forced cost is a
`SacrificePermanentCost` (the gain-control and remove-a-counter costs kill nothing); its completion sacrifices first
and only then clears the forced-cost state, and its defensive "chosen permanent is gone" branch, which resolves the
fallback effects, now closes the window first. `TormentSacrifice` sacrifices first and only then lets the paused
effect resume; its deferred branches (Torment of Hailfire, the each-player "unless discard" batches) only record the
pick and kill nothing, so they can't stop. Tests in `GravebaneZombieTest`: `forcedCostSacrificeAsksBeforeTheChosenZombieIsSacrificed`
(Pillar Tombs of Aku: no sacrifice log line while asked, no life lost after) and
`tormentSacrificeAsksBeforeTheChosenZombieIsSacrificed` (Indulgent Tormentor: no card drawn after). Still unreviewed
single-sacrifice context: `LandEquilibriumSacrifice` (runs inside a land-entering replacement, not an effect). Not
covered: `ForcedCostOrElseEffectHandler` itself (unmarked, so its lone-candidate mandatory sacrifice and the
lone-candidate sacrifice on the may prompt's "yes" still don't ask first), and the deferred Torment picks, which are
sacrificed later by their unmarked starting handlers.

**Also landed (2026-10-23, thirteenth pass).** `ForcedCostOrElseEffectHandler` is now marked `AsksDeathReplacementsFirst`,
but it closes the rerun window as it starts and reopens it only around the mandatory sacrifice of a lone candidate
for a `SacrificePermanentCost` (not a villainous choice, which records its extra choices first). Every step before
that sacrifice only reads (the payer, the candidates; re-pointing the stack entry at the enchanted creature's
current controller sets the same value again on the rerun), and every other path (other forced costs, the may
prompt, the fallback effects, the multi-candidate choice) resolves with the window closed. Test:
`GravebaneZombieTest.forcedCostLoneCandidateSacrificeAsksBeforeTheZombieIsSacrificed` (Eldrazi Monument with a
lone Zombie: no sacrifice log line while asked, the Monument stays and Kalitas makes no Vampire after). Still not
covered: the fallback effects' own deaths (e.g. "sacrifice this unless ..." when no candidate exists), the
lone-candidate sacrifice on the may prompt's "yes", and the deferred Torment picks.

**Also landed (2026-10-23, fourteenth pass).** Pulmonic Sliver's lone "may" is now asked inside the rerun window
too. `PermanentRemovalService.askDeathReplacementsBeforeRemoval` offers a creature whose only death replacement is a
granted "you may put it on top of its owner's library instead" through the usual may prompt (tracked in
`GameData.rerunMayLibraryChoices`) and stops the effect or choice completion, and `removePermanentToGraveyard` calls it
for a lone dying creature with that "may" (not only for competing replacements). `PutOnTopOfLibraryInsteadOfDyingHandler`
records the answer (`LIBRARY` or `NO_REPLACEMENT`) in `chosenDeathReplacementsForRerun` for the rerun, or completes
a stopped choice again once every answer is in. Tests in `PulmonicSliverTest`: `wrathAsksBeforeAnyCreatureIsDestroyed`
(both Slivers and a Bear stay until both answers are in, then leave together),
`sacrificeThenGainLifeAsksBeforeTheLoneSliverIsSacrificed` (Devour Flesh: no life gained while asked) and
`chosenSacrificeThenGainLifeAsksBeforeTheSliverIsSacrificed` (the replayed edict choice). Still not covered: the
fallback effects' own deaths, the lone-candidate sacrifice on a mayfx prompt's "yes", and the deferred Torment picks.

**Also landed (2026-10-23, fifteenth pass).** The lone-candidate sacrifice on a "you may sacrifice" prompt's "yes"
is now asked inside the rerun window too. The ten mayfx handlers whose multi-candidate choice already joined
`killsBeforeChangingAnything` (`MaySacrificePermanentForCounterSource/OrTapSource/OrRemoveSourceCounter`,
`GargantuanGorillaUpkeep`, `AnyOpponentMaySacrificeCreatureTapAndCounter/NontokenCreatureTapAndSeekLand/CreatureTapAndGainLifeAndDraw/CreatureOrTransform`,
`AnyPlayerMaySacrificeLandPutSourceOnTop` and `AnyPlayerMaySacrificeCreatureToCounterSpell`) no longer sacrifice
the lone candidate themselves: they build the same `PermanentChoiceContext` and hand it to
`PermanentChoiceHandlerService.completeLoneCandidateAskingDeathReplacementsFirst`, which runs the choice
completion with the window open and replays it after the answer, exactly as for a chosen candidate (each lone
path was the same sequence as its completion; the now-unused `MaySacrificeForCounterSupport.sacrificeThenAddCounter`
overloads are gone). Tests in `GravebaneZombieTest`: `anyOpponentLoneCandidateSacrificeAsksBeforeTheZombieIsSacrificed`
(Desecration Demon: untapped with no counter while asked, tapped with one after),
`maySacrificeForCounterLoneCandidateAsksBeforeTheZombieIsSacrificed` (High-Society Hunter: no counter while asked)
and `anyPlayerLoneCandidateSacrificeToCounterAsksBeforeTheZombieIsSacrificed` (Brain Gorgers: not countered while
asked). Still not covered: the fallback effects' own deaths, the deferred Torment picks, a collected-picks batch
ended by the last player's decline, multi-choice `ForcedSacrifice`, `LandEquilibriumSacrifice` and sacrifices
paid as costs.

**Also landed (2026-10-23, sixteenth pass).** A collected-picks batch ended by the last player's decline is now
asked inside the rerun window too. When the declined prompt already carries earlier players' picks, the mayfx
handlers `AnyOpponentMaySacrificeCreatureOrTransformSourceHandler` (Innocent Traveler) and
`AnyPlayerMaySacrificeCreatureToCounterSpellHandler` (Brain Gorgers) run their decline through
`PermanentChoiceHandlerService.declineAskingDeathReplacementsFirst`: the decline changes nothing before its
`performSimultaneousSacrifice`, a stop stores a `GameData.MayDeclineReplay` (the replay field is now the sealed
`DeathReplacementChoiceReplay`), and once every answer is in the same decline is dispatched again through the
may handler. Tests: `GravebaneZombieTest.anyPlayerLastDeclineAsksBeforeThePickedZombieIsSacrificed` (Brain Gorgers:
not countered while asked, countered once after) and `InnocentTravelerTest.lastDeclineAsksBeforeThePickedZombieIsSacrificed`
(three players; covers the replay, though nothing observable followed the sacrifice before either). Still not
covered: the fallback effects' own deaths, the deferred Torment picks, multi-choice `ForcedSacrifice`,
`LandEquilibriumSacrifice` and sacrifices paid as costs.

**Also landed (2026-10-23, seventeenth pass).** Multi-choice `MultiPermanentChoiceContext.ForcedSacrifice` completions
are now asked inside the rerun window too. Their completion only reads (the sacrificed power for `recordSacrificedPower`)
or prompts the next chooser, with the earlier picks kept in the immutable context, before it ends in
`performSimultaneousSacrifice`, which asks the whole batch first. `MultiPermanentChoiceHandlerService.completeForcedSacrificeAskingDeathReplacementsFirst`
runs it through `PermanentChoiceHandlerService.runAskingDeathReplacementsFirst` (now public); a stop stores a
`GameData.MultiPermanentChoiceReplay`, and once every answer is in the same answer is completed again. This covers
every `ForcedSacrifice` prompt (`SacrificePermanentsEffect` with a multi-select filter, the each-player fraction /
down-to / two-distinct-permanents handlers, `DestructionSupport.sacrificePlayerMatchingPermanents`, ...). Test:
`GravebaneZombieTest.forcedMultiSacrificeAsksBeforeAnyChosenCreatureIsSacrificed` (Barter in Blood: both players'
chosen creatures stay while asked; after, the Zombie is on top of the library, Kalitas exiles the Bears and makes one
Zombie, one sacrifice log line). Still not covered: the fallback effects' own deaths, the deferred Torment picks,
the other "sacrifice, then" multi-choices (the `EachPlayerSacrifices...`/`EachOpponentSacrifices...` multi contexts,
`SacrificeAttackingCreatures`, ...; `ForcedSacrificeThenDamageIfSubtype` only sacrifices lands),
`LandEquilibriumSacrifice` and sacrifices paid as costs.

**Also landed (2026-10-23, eighteenth pass).** The "each player/opponent sacrifices ..., then" multi-choice
completions joined the window. `MultiPermanentChoiceHandlerService.killsBeforeChangingAnything` lists the contexts
whose completion only reads (power, toughness, the chosen cards) or prompts the next chooser with the picks kept in
the immutable context before it sacrifices the whole batch through `performSimultaneousSacrifice`:
`ForcedSacrifice`, `EachPlayerSacrificesCreature` (Wasteland Raider, Rankle), `EachPlayerSacrificesCreatureCreateTokenEqualToTotalPower`
(Reign of the Pit), `DanseMacabreSacrifice`, `EachOpponentSacrificesCreatureCreateTokens` (Syphon Flesh),
`EachOpponentSacrificesPermanentCreateTokens` (Visions of Ruin), `EachOpponentSacrificesNontokenCreatureConjuresDuplicates`
(Replicating Terror), `ControllerAndTargetPlayerChooseCreaturesThenSacrifice` (Malik) and
`TargetPlayerChoosesCreatureOrPlaneswalkerThenSacrificesChosen` (Speedbrood Stalker; it sacrifices one at a time,
so only its first sacrifice can ask). `completeAskingDeathReplacementsFirst` (renamed from
`completeForcedSacrificeAskingDeathReplacementsFirst`) runs each completion and its epilogue in the window and is
also the replay target. Their starting handlers `DanseMacabreEffectHandler` and
`EachOpponentSacrificesNontokenCreatureConjuresDuplicatesEffectHandler` are now marked too (each only reads before
its sacrifice; Danse Macabre's return phase kills nothing). Tests in `GravebaneZombieTest`:
`eachOpponentSacrificeThenTokensAsksBeforeTheChosenZombieIsSacrificed` (Syphon Flesh: no Zombie token while asked,
one after) and `eachPlayerSacrificeThenTotalPowerTokenAsksBeforeAnyChosenCreatureIsSacrificed` (Reign of the Pit:
the caster's Bears stays and nothing is sacrificed while asked; a 5/5 Demon after). Still not covered: the fallback
effects' own deaths, the deferred Torment picks, the remaining sacrifice multi-choices (`SacrificeAttackingCreatures`,
`TargetPlayerSacrificesCreatureAndPlaneswalker`, `EachOpponentSacrificesArtifactAndNonartifactCreature`,
`KeepForEachPlayerThenSacrificeRest`, `PlayerChoosesUpToPermanentsThenSacrificesRestChoice`, `SacrificeDamagedPlayerControls`,
`WormsOfTheEarthSacrificeLands`, ...), `LandEquilibriumSacrifice` and sacrifices paid as costs.

**What the rules require.** CR 616.1: when several replacement effects would modify how a creature
dies, its controller chooses one, and the event then happens with that choice. The remaining gaps,
where the creature still stays on the battlefield until answered (`DeathExileReplacementChoice` with
neither `stateBased` nor `rerunsEffect`) while the effect carries on:
- Handlers not marked `AsksDeathReplacementsFirst`: the remaining larger sacrifice handlers (the plain destroy
  and sacrifice handlers are done apart from the ones listed above) and the larger mixed handlers
  that use `DestructionSupport` (list them with `grep -rlE "destructionSupport\.(destroyBatch|tryDestroyAndLog|performSimultaneousSacrifice|sacrificeAndLog)"`
  under `service/effect/`). Each needs review for "nothing before the first removal" before it is
  marked; a handler that removes through `performSimultaneousRemovals` must also call
  `askDeathReplacementsBeforeRemoval` for the batch first (that method can't do it itself, since it
  also runs for exiles and bounces).
- Deaths after an earlier change of the same handler (a second removal, damage, a regeneration or
  shield counter used, an indestructible log): the window closes, since a rerun would repeat them.
- Deaths in an interaction's completion other than the single-sacrifice and destroy contexts done in the sixth
  and seventh passes ("sacrifice, then" multi-choices other than those in
  `MultiPermanentChoiceHandlerService.killsBeforeChangingAnything`,
  `LandEquilibriumSacrifice` and the destroy completions listed in the seventh pass), and sacrifices paid as costs.

**Why it's large.** Approaches looked at:
- Leave the battlefield now and choose the destination later: the hand, library and graveyard
  pipelines fire different leave-the-battlefield triggers depending on the destination, so delaying
  them loses their look-back.
- Simulate the effect on a copy of `GameData` first: needs engine services to run on a copy
  mid-resolution, and still misses deaths that come after another prompt inside the effect.
- Stop and rerun (what landed): only safe for handlers proven to change nothing first. Extending it
  to choice completions re-dispatches the completion with the same answer after the death choice
  (done for single sacrifices; a multi-choice needs its accumulated selections replayed too), and
  to costs would need a rerun of the activation's cost payment.

**Prompt to start it:**

```
Continue missing-features-to-do.md entry 34: extend the CR 616.1 "ask before the creature leaves"
rerun window (AsksDeathReplacementsFirst, PermanentRemovalService.askDeathReplacementsBeforeRemoval,
GameData.chosenDeathReplacementsForRerun; for choice completions
PermanentChoiceHandlerService.completeAskingDeathReplacementsFirst) to the remaining choice completions
(the other "sacrifice, then" multi-choices, the unreviewed single-sacrifice contexts) and the remaining handlers,
reviewing each handler for changes before its first removal. Write a failing Kalitas + Gravebane
Zombie test per case first. Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 35. Choosing the order of token-creation replacement effects

**Status:** open · **Found:** 2026-10-18, while fixing Q-card test failures

**Failing tests:**
- `QueenAllenalOfRuadachTest.doesNotSilentlyForceTokenDoublingBeforeQueenReplacement()`

**What the rules require.** CR 616.1: when several replacement effects would change how tokens are
created for a player, that player chooses which one applies first, then the next, and so on
(CR 616.1e: any applicable one may be chosen). With Queen Allenal of Ruadach ("those tokens plus an
additional 1/1 Soldier") and Parallel Lives (twice that many) out, Captain's Call can make 8 Soldiers
(Queen first: (3+1)x2) or 7 (Parallel Lives first: 3x2+1). The engine always applies the multiplier
(`MultiplyTokenCreationEffect`) to the base amount and then adds the Queen/Frog/Squirrel-style
additions unmultiplied, in a fixed order with no prompt, so the player always gets 7.

**Why it's large.** Token creation doesn't go through `ReplacementOrderService`. The fixed order is
duplicated in three creation paths (`PermanentControlSupport` ~L120-240, `DestructionSupport` ~L1012,
`TokenCopySupport` ~L193) plus the additive helpers in `TokenCreationReplacementSupport`. A fix needs
a token-creation replacement event that collects every applicable multiplier and addition, a choice
prompt (or a single "order" choice) when more than one applies, and suspend/resume at each creation
site — some of which run mid-effect. Silently picking the "best" order is not acceptable: the
player must choose.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 35: let the player order token-creation replacement
effects (CR 616.1) — multipliers like Parallel Lives/Doubling Season and additions like Queen
Allenal of Ruadach, Quina Qu Gourmet, Chatterfang. Unify the fixed ordering in PermanentControlSupport,
DestructionSupport and TokenCopySupport behind one token-creation replacement event, and reuse the
ChoiceContext / ReplacementOrderService approach from the CR 616.1 death replacement ordering.
Make QueenAllenalOfRuadachTest.doesNotSilentlyForceTokenDoublingBeforeQueenReplacement pass.
Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 36. Quenchable Fire's pay-{U}-before-upkeep special action

**Status:** open · **Found:** 2026-10-18, while fixing Q-card test failures

**Failing tests:**
- None. Write one: after Quenchable Fire resolves, the targeted player pays {U} during the caster's
  end step (or any time they could cast an instant) and no damage is dealt at the caster's upkeep.

**What the rules require.** Quenchable Fire: "It deals an additional 3 damage to that player or
planeswalker at the beginning of your next upkeep step unless that player or that planeswalker's
controller pays {U} before that step." Its ruling: paying is a special action that player may take
any time they have priority before that upkeep starts; once the delayed trigger is on the stack it
can't be stopped. The engine now pushes the delayed damage trigger under the caster's control at
upkeep (`StepTriggerService`, `DamageAtNextUpkeepUnlessPays`), but there is no way to pay, so the
damage always happens.

**Why it's large.** It needs a new player action: a special action (no stack) offered while the
player has priority, listed in the action-availability service and the client UI, which pays the
mana and removes the matching `DamageAtNextUpkeepUnlessPays` delayed action.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 36: add a special action that lets the affected player
pay {U} before the caster's next upkeep to cancel Quenchable Fire's delayed damage
(DamageAtNextUpkeepUnlessPays). Offer it whenever that player has priority, through
GameActionAvailabilityService and the networking/frontend action list. Don't run the full test
suite; ask me. Don't commit until I say so.
```

---

## 37. Choosing modes (and tier costs) when casting a modal spell from a graveyard

**Status:** open · **Found:** 2026-10-18, while fixing Q-card test failures

**Failing tests:**
- `QuistisTrepeTest.tieredSpellRequiresChoosingAModeBeforeItIsCast()`

**What the rules require.** CR 601.2b: modes are announced as the spell is cast, and a tiered spell's
chosen tier adds its cost. Casting from hand does this (`SpellCastingService.playCard`, with
`tieredManaSuffix`). Casting from a graveyard (`MayCastHandlerService.handleCastCardFromGraveyardChoice`,
Quistis Trepe and other "you may cast target card from a graveyard" effects) announces no mode: the
spell goes on the stack with an unresolved `ChooseOneEffect` and pays no tier cost.

**Why it's large.** Needs a new graveyard-cast mode choice (a `ChoiceContext` next to
`LibraryFreeCastModeChoice` in `ChoiceHandlerService`), unwrapping the chosen mode via
`SpellCastingService.prepareModalSpellCast`, carrying a replacement mana cost for the tier through
`GraveyardCastSpellTarget` (many constructors) to both pay sites (`MayCastHandlerService` untargeted
pay and `PermanentChoiceSpellHandlerService.handleGraveyardCastSpellTarget`), and building targets
from the chosen mode's effects. Roughly 150-200 lines across 5-6 files.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 37: casting a modal or tiered spell from a graveyard
(MayCastHandlerService.handleCastCardFromGraveyardChoice) must announce the mode (CR 601.2b) and pay
the tier cost. Add a graveyard-cast mode choice next to LibraryFreeCastModeChoice, unwrap via
prepareModalSpellCast, pass the tiered replacement cost through GraveyardCastSpellTarget, and make
QuistisTrepeTest.tieredSpellRequiresChoosingAModeBeforeItIsCast pass. Don't run the full test suite;
ask me. Don't commit until I say so.
```

---

## 38. "Until end of turn" effects under Runed Terror's phase-by-phase turn structure

**Status:** open · **Found:** 2026-10-19, while fixing R-card test failures

**Failing tests:**
- `RunedTerrorTest.temporaryBoostSurvivesOtherPlayersCleanup()`

**What the rules require.** Runed Terror makes players take each phase in sequence instead of whole
turns, so every player gets a cleanup step during one turn cycle. Its ruling (2024-11-08, found by web
search; re-check before implementing)
says an "until end of turn" effect ends at the cleanup step of the player who was active when the
effect began. CR 514.2 ends "until end of turn" and "this turn" effects at cleanup. The engine's
`TurnCleanupService.applyCleanupResets` / `resetEndOfTurnModifiers` clears every temporary modifier at
whichever cleanup comes first, so Titanic Growth cast during player 2's phases ends at player 1's
cleanup.

**Why it's large.** Every until-end-of-turn store (power/toughness modifiers, granted keywords,
floating layer effects, "this turn" flags and sets) would need to record the active player at creation
and be cleared only at that player's cleanup while Runed Terror's structure is in effect. That touches
most of `TurnCleanupService` and every place that creates such effects.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 38: while RunedTerrorEffect is active, "until end of turn"
effects must end at the cleanup step of the player who was active when the effect was created
(Runed Terror ruling; CR 514.2). Record the creating active player on temporary modifiers / floating
effects and make TurnCleanupService.applyCleanupResets clear only those belonging to the player whose
cleanup it is. Make RunedTerrorTest.temporaryBoostSurvivesOtherPlayersCleanup pass without changing
normal-game behaviour. Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 39. Non-mana triggers from mana abilities are held during the mana-undo window

**Status:** open · **Found:** 2026-10-19, while fixing R-card test failures

**Failing tests:**
- `RelicPutrescenceTest.manaAbilityResolvesBeforePoisonTrigger()` ("Tapping for mana adds mana
  immediately and puts poison on the stack")

**What the rules require.** CR 605.3b: an activated mana ability resolves immediately. Relic
Putrescence's "Whenever enchanted artifact becomes tapped, its controller gets a poison counter" is
not a mana ability, so per CR 603.3 it goes on the stack the next time a player would receive
priority. The engine's `GameService.flushManaAbilityTriggersAfterActivation` deliberately keeps such
triggers in `pendingManaAbilityTriggers` while `revertableManaActivations` is non-empty, so the
player can still undo the mana tap. The trigger therefore is not on the stack after the tap.

**Why it's large.** It is a conflict between rules timing and the client's "undo mana tap" UX.
Fixing it means deciding that a mana activation whose trigger leaves the mana-ability world can't
be reverted (or ending the revert window at that point), then changing the revert bookkeeping, the
flush logic, and the frontend's undo affordance.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 39: when an activated mana ability causes a non-mana
triggered ability (e.g. Relic Putrescence), put that trigger on the stack at the next priority
(CR 603.3) instead of holding it in pendingManaAbilityTriggers during the revertable-mana window.
Decide how this interacts with revertableManaActivations (likely: that activation becomes
non-revertable) and update GameService.flushManaAbilityTriggersAfterActivation and the client undo
UI. Make RelicPutrescenceTest.manaAbilityResolvesBeforePoisonTrigger pass. Don't run the full test
suite; ask me. Don't commit until I say so.
```

---

## 40. Mana riders and spending restrictions tied to specific units of mana

**Status:** open · **Found:** 2026-10-19, while fixing S-card test failures

**Failing tests:**
- `SunkenPalaceTest.activatingAnotherManaAbilityDoesNotConsumeTheCopyRider()`
- `SunkenPalaceTest.freeSpellDoesNotConsumeTheCopyRider()`
- `SunkenPalaceTest.spellPaidWithOtherManaDoesNotTriggerACopy()`
- `SunkenCitadelTest` "Damping Sphere changes mana type and amount but preserves the spending restriction"

**What the rules require.** CR 106.6: a mana ability can restrict how its mana is spent, add an
effect to the spell or ability that mana is spent on, or create a delayed trigger that fires when
that mana is spent. Sunken Palace's {U} copies the spell or ability *that mana* is spent on, so the
copy must fire only when that exact unit pays for something. Sunken Citadel's two mana "spend this
mana only to activate abilities of land sources" keeps its restriction even when Damping Sphere
replaces it with different mana. The engine instead arms the Palace rider per player:
`ActivatedAbilityExecutionService` calls `TriggerCollectionService.consumeManaLinkedCopyOnManaAbility`
on any later mana ability, and the next spell is copied whichever mana paid for it. Citadel's
restriction is lost when a replacement changes the mana.

**Why it's large.** `ManaPool` tracks counts per color plus a few special pools. Riders and
restrictions have to stay attached to individual units through every payment path, cost
reduction, free cast, mana-doubling and mana-replacement effect, and spell copy. That touches the
pool, all of `ManaCost`'s payment helpers, and every caller that spends mana.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 40: tag mana units in ManaPool with their rider/spend
restriction (CR 106.6) so Sunken Palace's "when you spend this mana ... copy that spell or ability"
fires only for the spell or ability that the tagged {U} actually paid for, and Sunken Citadel's
land-ability-only restriction survives Damping Sphere-style mana replacement. Replace the per-player
consumeManaLinkedCopyOnManaAbility arming. Make the failing SunkenPalaceTest and SunkenCitadelTest
cases pass. Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 41. Casting spells and playing lands from another player's hand (Sen Triplets)

**Status:** open · **Found:** 2026-10-19, while fixing S-card test failures

**Failing tests:**
- `SenTripletsTest.controllerCanCastSpellFromOpponentsHand()`
- `SenTripletsTest.controllerCanPlayLandFromOpponentsHand()`

**What the rules require.** Sen Triplets (ARB 109): "You may play lands and cast spells from that
player's hand this turn." CR 601.2a: casting moves the card "from where it is" to the stack, and the
caster becomes its controller, so the card goes straight from the opponent's hand to the stack. The
owner doesn't change, and per CR 400.3 a spell cast this way goes to its owner's graveyard. A land
played this way enters under the Sen Triplets controller's control and uses that player's land play
(CR 305.2). The engine already records the window (`GameData.senControllerPlayerId` /
`senControlledPlayerId`), silences the opponent and reveals their hand, but no cast or land-play path
reads the window: every one takes an index into the acting player's own hand.

**Why it's large.** It cuts across modules:
- **Engine:** `SpellCastingService`'s hand cast paths (about 30 sites read `playerHands.get(playerId)`)
  and the land-play path need the hand owner as an explicit source zone, without moving the card
  through the caster's hand first.
- **Cost and trigger context:** everything that treats "from hand" as "from your hand" needs checking.
  Verify any "cast from your hand" interaction against official rulings before deciding it.
- **Playability:** the availability checks behind the UI highlighting and the AI must offer the
  opponent's cards.
- **Networking:** `PlayCardRequest` has no hand-owner field.
- **Frontend:** the revealed opponent hand has to become playable while the window is open.

A previous attempt that moved the card into the caster's hand before casting was reverted, because it
adds a zone change the rules don't have and leaves the card in the wrong hand when the cast stops for
a prompt.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 41: let the Sen Triplets controller cast spells and play
lands from the controlled opponent's hand while GameData.senControllerPlayerId/senControlledPlayerId
is set. Pass the hand owner explicitly through the hand cast and land-play paths so the card moves
directly from the opponent's hand to the stack/battlefield under the caster's control (CR 601.2a),
keeps its owner (CR 400.3), and uses the caster's mana and land play (CR 305.2). Don't route the card
through the caster's hand. Update playability checks (UI highlighting, AI), add a hand-owner field to
PlayCardRequest, and make the revealed opponent hand playable in the frontend. Make
SenTripletsTest.controllerCanCastSpellFromOpponentsHand and controllerCanPlayLandFromOpponentsHand
pass. Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 42. Aftermath cards are only their front half off the stack under MTGJSON

**Status:** open · **Found:** 2026-10-21, while adding the provider parity test for the stored set data

**Failing tests:**
- None under MTGJSON, which the test tasks prefer. Making the cards rules-correct breaks the
  `assertInGraveyard`/`assertInExile`/name-match assertions (about 80) in the 27 aftermath test
  classes, e.g. `CommitMemoryTest`, `StartFinishTest`, `SpringMindTest`, `ReasonBelieveTest`. Those
  same assertions should fail today for any set stored from Scryfall.
- `ProviderParityTest` (`scryfall-api`) tolerates this difference explicitly in
  `withoutAftermathNaming`; that exception should go once this is fixed.

**What the rules require.** Aftermath "is an ability found on some split cards" (CR 702.127a), so an
aftermath card is a split card. "In every zone except the stack, the characteristics of a split card
are those of its two halves combined" (CR 709.4): it has two names (CR 709.4a), and its mana value is
determined from its combined mana cost (CR 709.4b) — Commit // Memory has mana value 10 in the
graveyard. Scryfall lays these cards out as `split`, so `FaceOracleMapper` maps them to the whole card
("Commit // Memory", combined mana value). MTGJSON lays them out as `aftermath`, which the mapper's
name and combined-mana-value rules don't treat as split, so the card is just "Commit" with the front
half's mana value. With provider fallback, which provider a set was first stored from now decides how
these cards behave.

**Why it's large.** The fix itself is two lines (treat `isSplitLayout` rather than `"split"` in
`FaceOracleMapper.toOracleData`'s name and combined-mana-value rules, and in
`MtgjsonSetSource.toRawFace`'s naming). But the aftermath card classes are modelled as a front-face
class with a back-face class cast via `FlashbackCast`, and their tests look the card up by the
front-half name. Every such test needs updating, and the engine's name matching for these cards
(e.g. Failure // Comply's name choice, and anything checking "a card named X" per CR 709.4a) needs
checking against the combined name. `StoredSet.FORMAT_VERSION` must be bumped, since MTGJSON-stored
files carry the front-half name.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 42: make aftermath cards split cards off the stack under
both providers (CR 702.127a, 709.4, 709.4a, 709.4b). In FaceOracleMapper.toOracleData use
isSplitLayout(face.layout()) instead of "split".equals(...) for keeping the whole name and for
combinedManaValue, and in MtgjsonSetSource.toRawFace name an aftermath front by the whole card name.
Bump StoredSet.FORMAT_VERSION. Then fix the aftermath card tests (the 27 classes using Aftermath /
FlashbackCast back faces, e.g. CommitMemoryTest) that assert the front-half name, and verify that
name matching for these cards (choose-a-card-name effects, "card named X") treats either half's
name as the card's name. Remove the withoutAftermathNaming exception from ProviderParityTest and
check it passes for AKH. Verify every CR number with the rules MCP. Don't run the full test suite;
ask me. Don't commit until I say so.
```

---

## 43. Static abilities of an instant or sorcery while it's on the stack (Torrent of Lava)

**Status:** open · **Found:** 2026-10-22, while fixing T-card test failures

**Failing tests:**
- `TorrentOfLavaTest` "A creature can prevent one damage from Torrent of Lava while it is on the stack"
- `TorrentOfLavaTest` "The caster's creature can prevent damage, without protecting another creature"
- `TorrentOfLavaTest` "The granted ability prevents only one damage"

**What the rules require.** CR 113.6: abilities of an instant or sorcery spell function while that
object is on the stack. Torrent of Lava says "As long as Torrent of Lava is on the stack, each
creature has '{T}: Prevent the next 1 damage that Torrent of Lava would deal to this creature this
turn.'" The card registers that grant as an `EffectSlot.STATIC` effect, but the layer system only
collects static effects from permanents (plus the graveyard/scheme wrappers), so the granted
activated ability never exists while the spell is on the stack.

**Why it's large.** It needs a new "stack static" source in `LayerSystemService.collectInstances`
(similar to `GraveyardStaticEffect`/`WrappedGraveyardStaticEffect`), layer classification for it,
invalidation when the spell enters or leaves the stack, and a prevention shield keyed to the spell
as the damage source.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 43: let an instant or sorcery's static abilities apply
while it is on the stack (CR 113.6), so Torrent of Lava grants each creature "{T}: Prevent the next
1 damage that Torrent of Lava would deal to this creature this turn" only while it is on the stack.
Add a stack-static collection path to LayerSystemService alongside the graveyard static wrappers.
Make the three failing TorrentOfLavaTest cases pass. Verify every CR number with the rules MCP.
Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 44. Ordering competing mill replacement effects (The Water Crystal + Bruvac)

**Status:** open · **Found:** 2026-10-22, while fixing T-card test failures

**Failing tests:**
- `TheWaterCrystalTest.affectedOpponentChoosesOrderOfMillReplacementEffects()`

**What the rules require.** CR 616.1: when two or more replacement effects try to modify the same
event, the affected player chooses one to apply, then repeats for the rest. The Water Crystal ("If
an opponent would mill one or more cards, they mill that many cards plus four instead") and Bruvac
the Grandiloquent (doubling) give different totals depending on order, so the milling opponent must
choose. `GraveyardService.resolveMillPlayer` applies them in a fixed order with no prompt.

**Why it's large.** Mill isn't routed through `ReplacementOrderService`. It needs a mill event
object, candidate collection for every mill-modifying replacement, a resumable completion after the
choice, and migration of every mill caller onto the new path.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 44: route mill events through replacement ordering
(CR 616.1) so the affected player chooses the order of competing mill replacement effects such as
The Water Crystal and Bruvac the Grandiloquent. Add a mill event plus a resumable completion in
GraveyardService.resolveMillPlayer and reuse ReplacementOrderService. Make
TheWaterCrystalTest.affectedOpponentChoosesOrderOfMillReplacementEffects pass. Verify every CR
number with the rules MCP. Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 45. Optional additional votes from a static ability for every vote (Tivit)

**Status:** open · **Found:** 2026-10-22, while fixing T-card test failures

**Failing tests:**
- `TivitSellerOfSecretsTest.additionalVoteAppliesToAnotherCardsVote()`

**What the rules require.** CR 701.38a: to vote, each player chooses one of the options, starting
with a specified player and proceeding in turn order. Tivit, Seller of Secrets says "While voting,
you may vote an additional time", which applies to every vote its controller takes part in (Plea
for Power in the test), not only Tivit's own council's dilemma. The engine supports the extra vote
only inside `CouncilDilemmaEffectHandler`; `VotingSupport.countAdditionalVotes` counts only The
Valeyard's mandatory extra vote and never asks.

**Why it's large.** About 17 vote handlers each run their own voting loop. A generic optional extra
vote needs a static marker effect, a shared "vote again?" prompt in `VotingSupport`, and every vote
handler moved onto that shared loop.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 45: make "While voting, you may vote an additional time"
(Tivit, Seller of Secrets) work for every vote, not only council's dilemma. Add a static marker
effect and an optional extra-vote prompt to VotingSupport, and move the vote handlers onto the
shared voting loop. Make TivitSellerOfSecretsTest.additionalVoteAppliesToAnotherCardsVote pass and
keep The Valeyard's mandatory extra vote working. Verify every CR number with the rules MCP. Don't
run the full test suite; ask me. Don't commit until I say so.
```

---

## 46. Choosing which coin flip to ignore (Krark's Thumb)

**Status:** open · **Found:** 2026-10-22, while fixing T-card test failures

**Failing tests:**
- `TwoHeadedGiantTest.krarksThumbLetsControllerChooseWhichResultsToIgnore()`

**What the rules require.** Krark's Thumb: "If you would flip a coin, instead flip two coins and
ignore one." The controller sees both results and chooses which one to ignore. CR 705.3 covers
effects that dictate a flip's result. The engine's `CoinFlipService` treats the pair as "heads if
either is heads", which is right only for flips the player wants to win and gives no choice for
effects such as Two-Headed Giant's two separate flips, where the controller may prefer tails.

**Why it's large.** Every coin-flip effect resolves its flips synchronously. A player choice per
replaced flip needs a resumable prompt in the middle of each flipping effect, across all
coin-flip handlers.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 46: make Krark's Thumb flip two coins and let its
controller choose which result to ignore, instead of CoinFlipService auto-preferring heads. Add a
resumable choice to the coin-flip path used by every coin-flip handler. Make
TwoHeadedGiantTest.krarksThumbLetsControllerChooseWhichResultsToIgnore pass. Verify every CR number
with the rules MCP. Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 47. Per-slot target legality for "up to one target X, up to one target Y" spells

**Status:** open · **Found:** 2026-10-22, while fixing U-card test failures

**Failing tests:**
- `UrgentNecropsyTest.artifactTargetThatLosesArtifactTypeIsNotDestroyedAsACreature()`

**What the rules require.** Urgent Necropsy: "Destroy up to one target artifact, up to one target
creature, up to one target enchantment, and up to one target planeswalker." CR 608.2b says each
target is checked against the instance of the word "target" it was chosen for. The Plague Spores
example in that rule is this exact case: a target chosen only as "the artifact" that stops being an
artifact is illegal and must not be destroyed, even if it is now a creature. The card uses a single
`target(AnyOf, 0, 4)` group plus the
`AT_MOST_ONE_ARTIFACT_ONE_CREATURE_ONE_ENCHANTMENT_AND_ONE_PLANESWALKER` constraint. At resolution
each target is rechecked only against the combined AnyOf filter, so the target stays legal.

**Why it's large.** Splitting the card into four optional `target(..., 0, 1)` groups doesn't work.
Casting passes one flat `targetIds` list, and mapping it onto optional groups is positional. The
engine would need an explicit per-slot target assignment carried from casting to the stack entry,
across `TargetLegalityService`, `ValidTargetService`, the cast paths, the harness `cast*` overloads
and the AI target selector. `ChaoticTransformation`, `BoomBox`, `FinaleOfPromise` and
`ShredsOfSanity` use the same constraint approach and share the bug.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 47: record which slot ("target artifact", "target
creature", ...) each target of a constraint-based multi-slot spell was chosen for, and recheck each
target against its own slot at resolution (CR 608.2b). Start with Urgent Necropsy and make
UrgentNecropsyTest.artifactTargetThatLosesArtifactTypeIsNotDestroyedAsACreature pass while keeping
the existing "at most one creature" rejection test. Then apply it to Chaotic Transformation, Boom
Box, Finale of Promise and Shreds of Sanity. Verify every CR number with the rules MCP. Don't run the full test suite;
ask me. Don't commit until I say so.
```

---

## 48. Declining an optional player target group in a multi-group trigger

**Status:** open · **Found:** 2026-10-22, while fixing V-card test failures

**Failing tests:**
- `VolatileArsonistTest.canDeclineEveryTargetGroup(boolean)` (both parameterizations)

**What the rules require.** Volatile Arsonist: "Whenever this creature attacks, it deals 1 damage to
up to one target creature, up to one target player, and up to one target planeswalker." Each "up to
one" lets the controller choose zero targets for that instance of "target" (CR 601.2c, applied to
triggered abilities by CR 603.3d), so declining all three groups must be possible. The controller
may also choose themselves for the player group, which `canChooseControllerAsPlayerTarget` covers.

**Why it's large.** The trigger target prompts (`ETBTokenTargetService.processNextETBTokenMultiTargetTrigger`
and `PermanentChoiceTriggerHandlerService.handleETBTokenMultiTargetTrigger`) use a single-select
`PermanentChoice` and treat "choose your own player id" as "stop this group" only when the
controller is not a legal target for it. For a player group that allows any player, the same answer
means "target myself", so a single-slot optional player group can't be declined at all. Fixing it
needs a distinct decline input, for example routing optional single-slot groups through
`beginMultiPermanentOrPlayerChoice` (an empty selection declines). That changes the prompt type for
every multi-group trigger card and the tests and AI code that answer those prompts.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 48: give optional ("up to one") target groups of
multi-group triggered abilities a decline answer that is distinct from choosing the controller as a
player target. Start with Volatile Arsonist: make
VolatileArsonistTest.canDeclineEveryTargetGroup pass while keeping
canChooseControllerAsPlayerTarget passing. Find the other cards that go through
ETBTokenTargetService / PermanentChoiceTriggerHandlerService multi-target triggers and update their
tests and the AI target selection to the new prompt. Verify every CR number with the rules MCP.
Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 49. Playing cards from the top of another player's library

**Status:** open · **Found:** 2026-10-22, while fixing W-card test failures

**Failing tests:**
- `WindriddlePalacesTest.offersBothPlayersTopCardsWhenBothLibrariesAreNonempty()`

**What the rules require.** Windriddle Palaces: "You may play lands and cast spells from the top of
any player's library." Each player may play the top card of every library, not only their own. The
engine assumes a single library: `CastingPermissionService.findLibraryTopOwner` returns one owner,
the view's `playableLibraryTopCards` lists one card, and `PlayCardRequest` carries no library owner,
so a cast from the library top always uses that one library.

**Why it's large.** It is a protocol change across the stack: a list of library owners in the
playable-top-card view and in `canPlayLandFromTopOfLibrary`; a new `libraryOwnerId` field on
`PlayCardRequest` (many constructor overloads) threaded through `PlayCardRequestDispatchService` into
`SpellCastingService.playCardFromLibraryTop`; and frontend changes (`game.component.ts`,
`targeting-choice.service.ts`) so the UI sends which library the card comes from. Only showing both
cards in the view would let the UI offer the opponent's card but cast the player's own.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 49: let a player play cards from the top of any player's
library when an effect allows it (Windriddle Palaces). Make the playable-library-top view list every
eligible library's top card with its owner, add a library-owner id to PlayCardRequest and thread it
through PlayCardRequestDispatchService into SpellCastingService.playCardFromLibraryTop and the land
path, and update the Angular client to send it. Make
WindriddlePalacesTest.offersBothPlayersTopCardsWhenBothLibrariesAreNonempty pass and keep the other
play-from-top-of-library card tests (e.g. Future Sight, Courser of Kruphix, Bolas's Citadel) passing.
Verify every CR number with the rules MCP. Don't run the full test suite; ask me. Don't commit until
I say so.
```

---

## 51. Aura and Equipment restrictions modelled as abilities granted to the creature

**Status:** open · **Found:** 2026-10-23, auditing `GrantEffectEffect` attached scopes after the Gutter Shortcut fix

**Failing tests:** none known; the bug shows only when the creature loses its abilities (Dress Down, Humility,
Turn to Frog) or when "has" text is wrongly kept.

**What the rules require.** "Enchanted creature can't ..." / "Equipped creature can't ..." is a static ability of the
Aura or Equipment itself; removing the creature's abilities (layer 6, CR 613.1f) can't touch it. Only "has"/"gains"
text grants an ability to the creature. Wrapping the effect in `GrantEffectEffect(..., ENCHANTED_CREATURE /
EQUIPPED_CREATURE / ENCHANTED_PERMANENT)` models a granted ability. Gutter Shortcut was fixed (effect directly in
STATIC; `BlockLegalityService` now reads attached restrictions even after ability loss). Remaining cases:
- Card-only fixes (the engine already reads the effect from an attached permanent — `BlockLegalityService`
  restriction kinds and `GameQueryService.hasCantBeBlocked`/`hasAuraWithEffect`, which unwraps `ConditionalEffect`
  and `EnchantedPermanentConditionalEffect`):
  - `SecurityBypass` (SNC 59) — "As long as enchanted creature is attacking alone, it can't be blocked." Keep the
    granted connive trigger, which is a quoted ability.
  - `WayOfTheThief` (GTC 56) — "Enchanted creature can't be blocked as long as you control a Gate." Use
    `ConditionalEffect(cond, new CantBeBlockedEffect())`.
  - `ThievesTools` (AFR 122) — "Equipped creature can't be blocked as long as its power is 3 or less." Likely
    `EnchantedPermanentConditionalEffect(PermanentPowerAtMostPredicate(3), new CantBeBlockedEffect(), null)`; verify
    it works for Equipment.
  - `PsychicPaper` (WHO 181) and `BrotherhoodRegalia` (ACR 71) — "Equipped creature has ward ... and can't be
    blocked": only the ward is granted; "can't be blocked" is the Equipment's own restriction.
- Engine changes needed (these restriction kinds are only read from the creature's own or granted effects, see
  `GameQueryService` and `AuraAttachmentService`):
  - `TargetingRestrictionEffect` — `ArtifactWard` (ATQ 3, "can't be the target of abilities from artifact sources"),
    `AntiMagicAura` (5ED 72, "can't be the target of spells"), `SpectralShield` (ICE 302, "can't be the target of
    spells").
  - `CantBeEnchantedByOtherAurasEffect` — `AntiMagicAura`, `ConsecrateLand` (2ED 15; its indestructible is correctly
    granted).
  - `CantBeSacrificedEffect` — `AssaultSuit` (CMM 369).
  - `CantBecomeSuspectedEffect` — `AirtightAlibi` (MKM 149).
- The reverse mistake (granted keyword modelled as the attached permanent's own effect, so it survives ability
  loss): `TravelersCloak` (INV 83, "Enchanted creature has landwalk of the chosen type") and `VectisGloves` (MH2 241,
  "has artifact landwalk") put `CantBeBlockedIfDefenderControlsMatchingPermanentEffect` directly in STATIC. Landwalk
  is a keyword (CR 702.14c); wrap it in `GrantEffectEffect` like `TrailblazersBoots`.

**Why it's large.** Four restriction kinds need their attached-permanent lookups added in `GameQueryService`
(targeting legality is heavily shared), plus eleven card changes, each with a "loses all abilities" test.

**Prompt to start it:**

```
Fix the Aura/Equipment restriction modelling in missing-features-to-do.md entry 51. First the card-only fixes
(Security Bypass, Way of the Thief, Thieves' Tools, Psychic Paper, Brotherhood Regalia, Traveler's Cloak, Vectis
Gloves), then make GameQueryService/AuraAttachmentService read TargetingRestrictionEffect,
CantBeEnchantedByOtherAurasEffect, CantBeSacrificedEffect and CantBecomeSuspectedEffect from attached Auras and
Equipment, and convert Artifact Ward, Anti-Magic Aura, Spectral Shield, Consecrate Land, Assault Suit and Airtight
Alibi. Verify every Oracle text, and add a Dress Down (loses all abilities) test per card.
Don't run the full test suite; ask me. Don't commit until I say so.
```

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
