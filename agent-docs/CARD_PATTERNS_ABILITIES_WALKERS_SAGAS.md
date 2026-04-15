# Card Patterns: Activated Abilities, Planeswalkers & Sagas

All paths relative to `cards/`.

## Activated abilities

| Pattern | Reference | Notes |
|---------|-----------|-------|
| Tap to damage any | `p/ProdigalPyromancer.java` | `(true, null, DealDamageToAnyTargetEffect, true)` |
| Mana + tap to damage | `r/RodOfRuin.java` | `(true, "{3}", DealDamageToAnyTargetEffect, true)` |
| Damage + self-damage | `o/OrcishArtillery.java` | Two effects: damage target + DealDamageToControllerEffect |
| Pump self | `f/FurnaceWhelp.java` | `(false, "{R}", BoostSelfEffect, false)` |
| Pump self + conditional sacrifice | `d/DragonWhelp.java` | BoostSelfEffect activated ability + END_STEP_TRIGGERED ActivationCountConditionalEffect(threshold, abilityIndex, SacrificeSelfEffect) |
| Self unblockable (Phyrexian mana) | `t/TrespassingSouleater.java` | `(false, "{U/P}", MakeCreatureUnblockableEffect(true), ...)` — Phyrexian mana activated, self-targeting unblockable |
| Pump target | `g/GhostWarden.java` | `(true, null, BoostTargetCreatureEffect, true)` |
| Pump target + filter | `h/HateWeaver.java` | With PermanentPredicateTargetFilter |
| Tap to tap target | `i/IcyManipulator.java` | TapTargetPermanentEffect + PermanentPredicateTargetFilter |
| Tap to lock artifact | `r/RustTick.java` | MayNotUntapDuringUntapStepEffect (STATIC) + TapTargetPermanentEffect + PreventTargetUntapWhileSourceTappedEffect + PermanentIsArtifactPredicate |
| Tap to draw | `a/ArcanisTheOmnipotent.java` | `(true, null, DrawCardEffect, false)` |
| Draw + discard | `m/MerfolkLooter.java` | DrawCardEffect + DiscardCardEffect |
| Grant keyword to target | `m/MightWeaver.java` | GrantKeywordEffect with color filter |
| Sacrifice self for effect | `b/BottleGnomes.java` | SacrificeSelfCost + effect |
| Sacrifice self + multi-target creature damage | `f/FireShrineKeeper.java` | tap+mana+SacrificeSelfCost+DealDamageToTargetCreatureEffect(3) with multi-target ability constructor (0-2 creature targets) — "up to two target creatures" pattern |
| Sacrifice self + damage player/pw | `v/VulshokReplica.java` | SacrificeSelfCost + DealDamageToAnyTargetEffect + PermanentIsPlaneswalkerPredicate filter (restricts to players + planeswalkers) |
| Sacrifice self + choose source prevention | `a/AuriokReplica.java` | SacrificeSelfCost + PreventAllDamageFromChosenSourceEffect (prompts permanent choice on resolution) |
| Target prevention from chosen source + life gain | `h/HealingGrace.java` | PreventDamageToTargetFromChosenSourceEffect(3) + GainLifeEffect(3) — target chosen on cast, source chosen on resolution |
| Sacrifice subtype for effect | `s/SiegeGangCommander.java` | SacrificeSubtypeCreatureCost + DealDamageToAnyTargetEffect |
| Sacrifice artifact for effect | `b/BarrageOgre.java` | SacrificeArtifactCost + DealDamageToAnyTargetEffect (tap + sac artifact) |
| Sacrifice permanent (predicate) for effect | `m/MakeshiftMunitions.java` | SacrificePermanentCost(PermanentAnyOfPredicate) + DealDamageToAnyTargetEffect (mana + sac artifact or creature, no tap) |
| Sacrifice multiple permanents for tutor | `k/KuldothaForgemaster.java` | SacrificeMultiplePermanentsCost(3, PermanentIsArtifactPredicate) + SearchLibraryForCardTypesToBattlefieldEffect(ARTIFACT) (tap + sac 3 artifacts) |
| Tap to +1/+1 counters on controlled filtered permanents | `s/SteelOverseer.java` | `(true, null, PutPlusOnePlusOneCounterOnEachControlledPermanentEffect(PermanentAllOfPredicate(artifact+creature)))` — no target, affects all matching permanents you control |
| Regenerate (self) | `d/DrudgeSkeletons.java` | `(false, "{B}", RegenerateEffect, false)` |
| Regenerate (target creature) | `a/Asceticism.java` | `RegenerateEffect(true)` + PermanentPredicateTargetFilter |
| Static effect grant (own creatures) | `a/Asceticism.java` | `GrantEffectEffect(CantBeTargetOfSpellsOrAbilitiesEffect, GrantScope.OWN_CREATURES)` |
| Create token | `d/DragonRoost.java` | CreateTokenEffect |
| Mill target | `m/Millstone.java` | `(true, "{2}", MillTargetPlayerEffect, true)` |
| Mana dork (tap for color) | `b/BirdsOfParadise.java` | `(true, null, AwardAnyColorManaEffect, false)` |
| Mana rock (tap for N of any color) | `g/GildedLotus.java` | `(true, null, AwardAnyColorManaEffect(3), false)` |
| Mana rock (choose subtype + tap for any color restricted to chosen creature type) | `p/PillarOfOrigins.java` | ON_ENTER_BATTLEFIELD ChooseSubtypeOnEnterEffect + `(true, null, AwardAnyColorChosenSubtypeCreatureManaEffect, false)` |
| Mana dork (ON_TAP) | `l/LlanowarElves.java` | addEffect(ON_TAP, AwardManaEffect) |
| Animate self | `c/ChimericStaff.java` | AnimateSelfEffect |
| Sorcery-speed ability | `t/ThrullSurgeon.java` | ActivationTimingRestriction.SORCERY_SPEED |
| Metalcraft exile-flicker | `a/ArgentSphinx.java` | ExileSelfAndReturnAtEndStepEffect + METALCRAFT restriction |
| Metalcraft tap ability | `v/VedalkenCertarch.java` | ActivationTimingRestriction.METALCRAFT + target filter |
| Power-gated tap ability | `b/BloodshotTrainee.java` | ActivationTimingRestriction.POWER_4_OR_GREATER + creature target filter |
| Attack-only ability | `a/AncientHellkite.java` | ActivationTimingRestriction.ONLY_WHILE_ATTACKING + opponent creature filter (PermanentNotPredicate(PermanentControlledBySourceControllerPredicate)) |
| X-cost mass destroy (combat damage gated) | `s/SteelHellkite.java` | BoostSelfEffect pump + DestroyNonlandPermanentsWithManaValueXDealtCombatDamageEffect with maxActivationsPerTurn=1 — X-cost, once per turn, only affects damaged player's permanents |
| Loyalty (no target) | `a/AjaniOutlandChaperone.java` | `(+1, effects, false, description)` |
| Loyalty (with target filter) | `a/AjaniOutlandChaperone.java` | `(-2, effects, true, description, filter)` |

## Planeswalkers

| Pattern | Reference | Notes |
|---------|-----------|-------|
| Planeswalker with discard/sacrifice/pile separation | `l/LilianaOfTheVeil.java` | +1 EachPlayerDiscardsEffect, -2 SacrificeCreatureEffect (targets player), -6 SeparatePermanentsIntoPilesAndSacrificeEffect |
| Planeswalker with opponent reveal choice + silver counter exile + artifact-scaling token | `k/KarnScionOfUrza.java` | +1 KarnScionRevealTwoOpponentChoosesEffect, -1 KarnScionReturnSilverCounterCardEffect, -2 CreateTokenEffect with STATIC BoostSelfPerControlledPermanentEffect(1,1,PermanentIsArtifactPredicate) via tokenEffects map |
| Planeswalker with draw + delayed untap + tuck + emblem | `t/TeferiHeroOfDominaria.java` | +1 DrawCardEffect + RegisterDelayedUntapPermanentsEffect(2), -3 PutTargetPermanentIntoLibraryNFromTopEffect(2) with nonland filter, -8 TeferiHeroEmblemEffect. Emblem triggers ExileTargetOpponentPermanentOnDrawEffect on each draw |
| Variable loyalty (-X) | `c/ChandraNalaar.java` | `ActivatedAbility.variableLoyaltyAbility(effects, desc, filter)` — loyalty cost is -X chosen by player, X stored in xValue |
| Loyalty + mana + player damage | `c/ChandraBoldPyromancer.java` | +1 with AwardManaEffect + DealDamageToTargetPlayerEffect, -3 DealDamageToTargetCreatureOrPlaneswalkerEffect, -7 DealDamageToTargetPlayerEffect + DealDamageToAllCreaturesAndPlaneswalkersTargetControlsEffect |
| Multi-target tap ability (equip mover) | `b/BrassSquire.java` | `ActivatedAbility(true, null, effects, desc, multiTargetFilters, 2, 2)` — tap to attach Equipment to creature, instant speed, uses `AttachTargetEquipmentToTargetCreatureEffect` |
| Transform DFC | `b/BloodlineKeeper.java` | Front face with `setBackFaceCard(new LordOfLineage())` + `getBackFaceClassName()` override. Activated ability with `TransformSelfEffect` + subtype count restriction (`CardSubtype.VAMPIRE, 5`). Back face is a separate Card subclass (`LordOfLineage`) |
| Werewolf transform DFC | `d/DaybreakRanger.java` | Innistrad werewolf pattern: front face uses EACH_UPKEEP_TRIGGERED with `NoSpellsCastLastTurnConditionalEffect(TransformSelfEffect())`. Back face (`NightfallPredator`) uses EACH_UPKEEP_TRIGGERED with `TwoOrMoreSpellsCastLastTurnConditionalEffect(TransformSelfEffect())`. Both faces have their own activated abilities |
| Werewolf lord DFC | `i/InstigatorGang.java` | Werewolf transform + STATIC `StaticBoostEffect(power, 0, OWN_CREATURES, PermanentIsAttackingPredicate())` on both faces (front +1/+0, back +3/+0). Back face (`WildbloodPack`) also has Trample (auto-loaded from Scryfall) |
| Upkeep reveal transform DFC | `d/DelverOfSecrets.java` | Non-werewolf upkeep transform: front face uses UPKEEP_TRIGGERED with `LookAtTopCardMayRevealTypeTransformEffect(Set.of(INSTANT, SORCERY))`. Looks at top card, if matching type offers may reveal + transform. Back face (`InsectileAberration`) is vanilla (no transform back trigger) |
| Enchantment-to-land transform DFC (attack trigger) | `l/LegionsLanding.java` | Front face: ON_ENTER_BATTLEFIELD with CreateTokenEffect (1/1 Vampire with lifelink). ON_ALLY_CREATURES_ATTACK with MinimumAttackersConditionalEffect(3, TransformSelfEffect()). Back face (`AdantoTheFirstFort`): two ActivatedAbilities — tap for {W} (AwardManaEffect) and {2}{W},{T} for CreateTokenEffect. Pattern: enchantment that transforms into utility land when attacking with 3+ creatures |
| Enchantment-to-land transform DFC (end step trigger) | `g/GrowingRitesOfItlimoc.java` | Front face: ON_ENTER_BATTLEFIELD with LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect(4). CONTROLLER_END_STEP_TRIGGERED with ControlsPermanentCountConditionalEffect(4, PermanentIsCreaturePredicate, TransformSelfEffect()). Back face (`ItlimocCradleOfTheSun`): two mana abilities — tap for {G} and tap for {G} per creature. Pattern: enchantment that transforms into mana land at end step with 4+ creatures |
| Artifact-to-land transform DFC (counter threshold + tokens) | `t/TreasureMap.java` | Front face: ActivatedAbility({1}, tap, [ScryEffect(1), PutCounterOnSelfThenTransformIfThresholdEffect(LANDMARK, 3, false, [CreateTokenEffect.ofTreasureToken(3)])]). Back face (`TreasureCove`): two ActivatedAbilities — tap for {C} and {T}+SacrificePermanentCost(TREASURE) for DrawCardEffect. Pattern: artifact with scry+counter activated ability that transforms into utility land and creates tokens on transform |
| Artifact transform DFC (life threshold) | `c/ChaliceOfLife.java` | Front face: ActivatedAbility(tap, [GainLifeEffect(1), ControllerLifeThresholdConditionalEffect(30, TransformSelfEffect())]). Back face (`ChaliceOfDeath`): ActivatedAbility(tap, [TargetPlayerLosesLifeEffect(5)]). Pattern: artifact that gains life and transforms when controller has 30+ life; back face deals 5 damage via targeted tap ability |

## Sagas

| Pattern | Reference Card | Key Code |
|---------|-----------|-------|
| 3-chapter Saga with damage/life/token | `c/ChainersTorment.java` | SAGA_CHAPTER_I/II: DealDamageToEachOpponentEffect(2) + GainLifeEffect(2). SAGA_CHAPTER_III: CreateTokenFromHalfLifeTotalAndDealDamageEffect. Lore counters auto-managed: ETB adds 1st in StackResolutionService, precombat main adds subsequent in StepTriggerService. Sacrifice SBA in StateBasedActionService when lore >= final chapter and no chapter ability on stack |
| 3-chapter Saga with mass destruction + graveyard return | `f/FallOfTheThran.java` | SAGA_CHAPTER_I: DestroyAllPermanentsEffect(PermanentIsLandPredicate). SAGA_CHAPTER_II/III: EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(2, CardTypePredicate(LAND)). Each player returns up to 2 lands; auto-returns when <= maxCount, queues graveyard choices when > maxCount |
| 3-chapter Saga with targeting chapter + board wipe + graveyard exile | `p/PhyrexianScriptures.java` | SAGA_CHAPTER_I: PutPlusOnePlusOneCounterOnTargetCreatureEffect(1) + AddCardTypeToTargetPermanentEffect(ARTIFACT, true). Chapter I targets "up to one creature" — uses pending saga chapter target queue. SAGA_CHAPTER_II: DestroyAllPermanentsEffect(AllOf(IsCreature, Not(IsArtifact))). SAGA_CHAPTER_III: ExileAllOpponentsGraveyardsEffect. First Saga with chapter targeting |
| 3-chapter Saga with opponent sacrifice + discard + graveyard reanimate | `t/TheEldestReborn.java` | SAGA_CHAPTER_I: EachOpponentSacrificesPermanentsEffect(1, AnyOf(IsCreature, IsPlaneswalker)). SAGA_CHAPTER_II: EachOpponentDiscardsEffect(). SAGA_CHAPTER_III: ReturnCardFromGraveyardEffect.builder().destination(BATTLEFIELD).source(ALL_GRAVEYARDS).filter(CardAnyOfPredicate(creature, planeswalker)).build() |
| 3-chapter Saga with opponent-only targeting + tap lock + mass bounce | `t/TimeOfIce.java` | SAGA_CHAPTER_I/II: TapTargetPermanentEffect + PreventTargetUntapWhileSourceOnBattlefieldEffect. Uses `setSagaChapterTargetFilter()` with opponent-only filter. SAGA_CHAPTER_III: ReturnCreaturesToOwnersHandEffect(PermanentIsTappedPredicate). Per-chapter target filters via `sagaChapterTargetFilters` |
| 3-chapter Saga with "until your next turn" ability grant + counters + keywords | `s/SongOfFreyalise.java` | SAGA_CHAPTER_I/II: GrantActivatedAbilityEffect(mana ability, OWN_CREATURES, null, UNTIL_YOUR_NEXT_TURN). Grants tap-for-any-color ability to all creatures at resolution. SAGA_CHAPTER_III: PutPlusOnePlusOneCounterOnEachOwnCreatureEffect + GrantKeywordEffect(VIGILANCE, TRAMPLE, INDESTRUCTIBLE, OWN_CREATURES) |
| 3-chapter Saga with greatest-power targeting + counters + keyword grant | `t/TriumphOfGerrard.java` | SAGA_CHAPTER_I/II: PutPlusOnePlusOneCounterOnTargetCreatureEffect(1, PermanentHasGreatestPowerAmongControlledCreaturesPredicate()). SAGA_CHAPTER_III: GrantKeywordEffect(FLYING, FIRST_STRIKE, LIFELINK, TARGET, predicate) |
| 3-chapter Saga with graveyard targeting chapters + spell copy | `t/TheMirariConjecture.java` | SAGA_CHAPTER_I: ReturnCardFromGraveyardEffect(HAND, instant). SAGA_CHAPTER_II: ReturnCardFromGraveyardEffect(HAND, sorcery). Uses `canTargetGraveyard()` for graveyard target selection. SAGA_CHAPTER_III: GrantInstantSorceryCopyUntilEndOfTurnEffect |
