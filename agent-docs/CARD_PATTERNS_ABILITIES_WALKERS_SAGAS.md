# Card Patterns: Activated Abilities, Planeswalkers & Sagas

All paths relative to `cards/`.

## Activated abilities

| Pattern | Reference | Notes |
|---------|-----------|-------|
| Tap to damage any | `p/ProdigalPyromancer.java` | `(true, null, DealDamageToAnyTargetEffect, true)` |
| Mana + tap to damage | `r/RodOfRuin.java` | `(true, "{3}", DealDamageToAnyTargetEffect, true)` |
| Damage + self-damage | `o/OrcishArtillery.java` | Two effects: damage target + DealDamageToPlayersEffect(3, DamageRecipient.CONTROLLER) |
| Pump self | `f/FurnaceWhelp.java` | `(false, "{R}", BoostSelfEffect, false)` |
| Pump self + conditional sacrifice | `d/DragonWhelp.java` | BoostSelfEffect activated ability + END_STEP_TRIGGERED ConditionalEffect(new ActivationCount(threshold, abilityIndex), SacrificeSelfEffect) |
| Self unblockable (Phyrexian mana) | `t/TrespassingSouleater.java` | `(false, "{U/P}", MakeCreatureUnblockableEffect(true), ...)` — Phyrexian mana activated, self-targeting unblockable |
| Pump target | `g/GhostWarden.java` | `(true, null, BoostTargetCreatureEffect, true)` |
| Pump target + filter | `h/HateWeaver.java` | With PermanentPredicateTargetFilter |
| Tap to tap target | `i/IcyManipulator.java` | TapPermanentsEffect(TapUntapScope.TARGET) + PermanentPredicateTargetFilter |
| Tap to lock artifact | `r/RustTick.java` | MayNotUntapDuringUntapStepEffect (STATIC) + TapPermanentsEffect(TapUntapScope.TARGET) + DoesntUntapEffect.targetWhileSourceTapped() + PermanentIsArtifactPredicate |
| Tap to draw | `a/ArcanisTheOmnipotent.java` | `(true, null, DrawCardEffect, false)` |
| Draw + discard | `m/MerfolkLooter.java` | DrawCardEffect + DiscardEffect(1, CONTROLLER) |
| Grant keyword to target | `m/MightWeaver.java` | GrantKeywordEffect with color filter |
| Sacrifice self for effect | `b/BottleGnomes.java` | SacrificeSelfCost + effect |
| Sacrifice self + multi-target creature damage | `f/FireShrineKeeper.java` | tap+mana+SacrificeSelfCost+DealDamageToTargetCreatureEffect(3) with multi-target ability constructor (0-2 creature targets) — "up to two target creatures" pattern |
| Sacrifice self + damage player/pw | `v/VulshokReplica.java` | SacrificeSelfCost + DealDamageToAnyTargetEffect + PermanentIsPlaneswalkerPredicate filter (restricts to players + planeswalkers) |
| Sacrifice self + choose source prevention | `a/AuriokReplica.java` | SacrificeSelfCost + PreventAllDamageFromChosenSourceEffect (prompts permanent choice on resolution) |
| Target prevention from chosen source + life gain | `h/HealingGrace.java` | PreventDamageToTargetFromChosenSourceEffect(3) + GainLifeEffect(3) — target chosen on cast, source chosen on resolution |
| Sacrifice subtype for effect | `s/SiegeGangCommander.java` | SacrificePermanentCost(PermanentAllOfPredicate(creature + PermanentHasSubtypePredicate(GOBLIN)), description, false) + DealDamageToAnyTargetEffect |
| Sacrifice subtype for +1/+1 counters on target | `d/DerangedOutcast.java` | SacrificePermanentCost(PermanentAllOfPredicate(creature + PermanentHasSubtypePredicate(HUMAN)), description, false) + PutCounterOnTargetPermanentEffect(PLUS_ONE_PLUS_ONE, 2) — source is itself a Human, so it can sacrifice itself |
| Sacrifice artifact for effect | `b/BarrageOgre.java` | SacrificeArtifactCost + DealDamageToAnyTargetEffect (tap + sac artifact) |
| Sacrifice permanent (predicate) for effect | `m/MakeshiftMunitions.java` | SacrificePermanentCost(PermanentAnyOfPredicate) + DealDamageToAnyTargetEffect (mana + sac artifact or creature, no tap) |
| Sacrifice multiple permanents for tutor | `k/KuldothaForgemaster.java` | SacrificeMultiplePermanentsCost(3, PermanentIsArtifactPredicate) + SearchLibraryEffect(CardTypePredicate(ARTIFACT), LibrarySearchDestination.BATTLEFIELD) (tap + sac 3 artifacts) |
| Tap to +1/+1 counters on controlled filtered permanents | `s/SteelOverseer.java` | `(true, null, PutCounterOnEachControlledPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1, PermanentAllOfPredicate(artifact+creature)))` — no target, affects all matching permanents you control |
| Regenerate (self) | `d/DrudgeSkeletons.java` | `(false, "{B}", RegenerateEffect, false)` |
| Regenerate (target creature) | `a/Asceticism.java` | `RegenerateEffect(true)` + PermanentPredicateTargetFilter |
| Static effect grant (own creatures) | `a/Asceticism.java` | `GrantEffectEffect(TargetingRestrictionEffect.hexproof(), GrantScope.OWN_CREATURES)` |
| Create token | `d/DragonRoost.java` | CreateTokenEffect |
| Mill target | `m/Millstone.java` | `(true, "{2}", MillEffect(2, TARGET_PLAYER), true)` |
| Mana dork (tap for color) | `b/BirdsOfParadise.java` | `(true, null, AwardAnyColorManaEffect, false)` |
| Mana rock (tap for N of any color) | `g/GildedLotus.java` | `(true, null, AwardAnyColorManaEffect(3), false)` |
| Mana rock (choose subtype + tap for any color restricted to chosen creature type) | `p/PillarOfOrigins.java` | ON_ENTER_BATTLEFIELD ChooseSubtypeOnEnterEffect + `(true, null, AwardAnyColorChosenSubtypeCreatureManaEffect, false)` |
| Mana dork (ON_TAP) | `l/LlanowarElves.java` | addEffect(ON_TAP, AwardManaEffect) |
| Animate self (X/X) | `c/ChimericStaff.java` | AnimatePermanentsEffect(XValue, XValue, subtypes, …, SELF, UEOT) |
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
| Planeswalker with discard/sacrifice/pile separation | `l/LilianaOfTheVeil.java` | +1 DiscardEffect(1, EACH_PLAYER), -2 SacrificePermanentsEffect(1, PermanentIsCreaturePredicate, TARGET_PLAYER), -6 SeparatePermanentsIntoPilesAndSacrificeEffect |
| Planeswalker with opponent reveal choice + silver counter exile + artifact-scaling token | `k/KarnScionOfUrza.java` | +1 KarnScionRevealTwoOpponentChoosesEffect, -1 KarnScionReturnSilverCounterCardEffect, -2 CreateTokenEffect with STATIC BoostSelfEffect(PermanentCount(PermanentIsArtifactPredicate, CONTROLLER), same) via tokenEffects map |
| Planeswalker with draw + delayed untap + tuck + emblem | `t/TeferiHeroOfDominaria.java` | +1 DrawCardEffect + RegisterDelayedUntapPermanentsEffect(2), -3 PutTargetPermanentIntoLibraryNFromTopEffect(2) with nonland filter, -8 TeferiHeroEmblemEffect. Emblem triggers ExileTargetOpponentPermanentOnDrawEffect on each draw |
| Variable loyalty (-X) | `c/ChandraNalaar.java` | `ActivatedAbility.variableLoyaltyAbility(effects, desc, filter)` — loyalty cost is -X chosen by player, X stored in xValue |
| Loyalty + mana + player damage | `c/ChandraBoldPyromancer.java` | +1 with AwardManaEffect + DealDamageToPlayersEffect(2, TARGET_PLAYER), -3 DealDamageToTargetCreatureOrPlaneswalkerEffect, -7 DealDamageToPlayersEffect(10, TARGET_PLAYER) + DealDamageToAllCreaturesAndPlaneswalkersTargetControlsEffect |
| Multi-target tap ability (equip mover) | `b/BrassSquire.java` | `ActivatedAbility(true, null, effects, desc, multiTargetFilters, 2, 2)` — tap to attach Equipment to creature, instant speed, uses `AttachTargetEquipmentToTargetCreatureEffect` |
| Transform DFC | `b/BloodlineKeeper.java` | Front face with `setBackFaceCard(new LordOfLineage())` + `getBackFaceClassName()` override. Activated ability with `TransformSelfEffect` + subtype count restriction (`CardSubtype.VAMPIRE, 5`). Back face is a separate Card subclass (`LordOfLineage`) |
| Werewolf transform DFC | `d/DaybreakRanger.java` | Innistrad werewolf pattern: front face uses EACH_UPKEEP_TRIGGERED with `ConditionalEffect(new NoSpellsCastLastTurn(), TransformSelfEffect())`. Back face (`NightfallPredator`) uses EACH_UPKEEP_TRIGGERED with `ConditionalEffect(new TwoOrMoreSpellsCastLastTurn(), TransformSelfEffect())`. Both faces have their own activated abilities |
| Werewolf lord DFC | `i/InstigatorGang.java` | Werewolf transform + STATIC `StaticBoostEffect(power, 0, OWN_CREATURES, PermanentIsAttackingPredicate())` on both faces (front +1/+0, back +3/+0). Back face (`WildbloodPack`) also has Trample (auto-loaded from Scryfall) |
| Upkeep reveal transform DFC | `d/DelverOfSecrets.java` | Non-werewolf upkeep transform: front face uses UPKEEP_TRIGGERED with `LookAtTopCardMayRevealTypeTransformEffect(Set.of(INSTANT, SORCERY))`. Looks at top card, if matching type offers may reveal + transform. Back face (`InsectileAberration`) is vanilla (no transform back trigger) |
| Enchantment-to-land transform DFC (attack trigger) | `l/LegionsLanding.java` | Front face: ON_ENTER_BATTLEFIELD with CreateTokenEffect (1/1 Vampire with lifelink). ON_ALLY_CREATURES_ATTACK with ConditionalEffect(new MinimumAttackers(3), TransformSelfEffect()). Back face (`AdantoTheFirstFort`): two ActivatedAbilities — tap for {W} (AwardManaEffect) and {2}{W},{T} for CreateTokenEffect. Pattern: enchantment that transforms into utility land when attacking with 3+ creatures |
| Enchantment-to-land transform DFC (end step trigger) | `g/GrowingRitesOfItlimoc.java` | Front face: ON_ENTER_BATTLEFIELD with LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect(4). CONTROLLER_END_STEP_TRIGGERED with ConditionalEffect(new ControlsPermanentCount(4, PermanentIsCreaturePredicate), TransformSelfEffect()). Back face (`ItlimocCradleOfTheSun`): two mana abilities — tap for {G} and tap for {G} per creature. Pattern: enchantment that transforms into mana land at end step with 4+ creatures |
| Artifact-to-land transform DFC (counter threshold + tokens) | `t/TreasureMap.java` | Front face: ActivatedAbility({1}, tap, [ScryEffect(1), PutCounterOnSelfThenTransformIfThresholdEffect(LANDMARK, 3, false, [CreateTokenEffect.ofTreasureToken(3)])]). Back face (`TreasureCove`): two ActivatedAbilities — tap for {C} and {T}+SacrificePermanentCost(TREASURE) for DrawCardEffect. Pattern: artifact with scry+counter activated ability that transforms into utility land and creates tokens on transform |
| Artifact transform DFC (life threshold) | `c/ChaliceOfLife.java` | Front face: ActivatedAbility(tap, [GainLifeEffect(1), ConditionalEffect(new ControllerLifeAtLeast(30), TransformSelfEffect())]). Back face (`ChaliceOfDeath`): ActivatedAbility(tap, [LoseLifeEffect(5, TARGET_PLAYER)]). Pattern: artifact that gains life and transforms when controller has 30+ life; back face deals 5 damage via targeted tap ability |

## Sagas

| Pattern | Reference Card | Key Code |
|---------|-----------|-------|
| 3-chapter Saga with damage/life/token | `c/ChainersTorment.java` | SAGA_CHAPTER_I/II: DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT) + GainLifeEffect(2). SAGA_CHAPTER_III: CreateTokenFromHalfLifeTotalAndDealDamageEffect. Lore counters auto-managed: ETB adds 1st in StackResolutionService, precombat main adds subsequent in StepTriggerService. Sacrifice SBA in StateBasedActionService when lore >= final chapter and no chapter ability on stack |
| 3-chapter Saga with mass destruction + graveyard return | `f/FallOfTheThran.java` | SAGA_CHAPTER_I: DestroyAllPermanentsEffect(PermanentIsLandPredicate). SAGA_CHAPTER_II/III: EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(2, CardTypePredicate(LAND)). Each player returns up to 2 lands; auto-returns when <= maxCount, queues graveyard choices when > maxCount |
| 3-chapter Saga with targeting chapter + board wipe + graveyard exile | `p/PhyrexianScriptures.java` | SAGA_CHAPTER_I: PutCounterOnTargetPermanentEffect(PLUS_ONE_PLUS_ONE, 1) + AddCardTypeToTargetPermanentEffect(ARTIFACT, true). Chapter I targets "up to one creature" — uses pending saga chapter target queue. SAGA_CHAPTER_II: DestroyAllPermanentsEffect(AllOf(IsCreature, Not(IsArtifact))). SAGA_CHAPTER_III: ExileGraveyardCardsEffect(GraveyardExileScope.ALL_OPPONENTS). First Saga with chapter targeting |
| 3-chapter Saga with opponent sacrifice + discard + graveyard reanimate | `t/TheEldestReborn.java` | SAGA_CHAPTER_I: SacrificePermanentsEffect(1, AnyOf(IsCreature, IsPlaneswalker), EACH_OPPONENT). SAGA_CHAPTER_II: DiscardEffect(1, EACH_OPPONENT). SAGA_CHAPTER_III: ReturnCardFromGraveyardEffect.builder().destination(BATTLEFIELD).source(ALL_GRAVEYARDS).filter(CardAnyOfPredicate(creature, planeswalker)).build() |
| 3-chapter Saga with opponent-only targeting + tap lock + mass bounce | `t/TimeOfIce.java` | SAGA_CHAPTER_I/II: TapPermanentsEffect(TapUntapScope.TARGET) + DoesntUntapEffect.targetWhileSourceOnBattlefield(). Uses `setSagaChapterTargetFilter()` with opponent-only filter. SAGA_CHAPTER_III: ReturnToHandEffect.allPermanentsMatching(AllOf(PermanentIsCreaturePredicate, PermanentIsTappedPredicate)). Per-chapter target filters via `sagaChapterTargetFilters` |
| 3-chapter Saga with "until your next turn" ability grant + counters + keywords | `s/SongOfFreyalise.java` | SAGA_CHAPTER_I/II: GrantActivatedAbilityEffect(mana ability, OWN_CREATURES, null, UNTIL_YOUR_NEXT_TURN). Grants tap-for-any-color ability to all creatures at resolution. SAGA_CHAPTER_III: PutCounterOnEachControlledPermanentEffect(PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate()) + GrantKeywordEffect(VIGILANCE, TRAMPLE, INDESTRUCTIBLE, OWN_CREATURES) |
| 3-chapter Saga with greatest-power targeting + counters + keyword grant | `t/TriumphOfGerrard.java` | SAGA_CHAPTER_I/II: PutCounterOnTargetPermanentEffect.withTargetRestriction(PLUS_ONE_PLUS_ONE, 1, PermanentHasGreatestPowerAmongControlledCreaturesPredicate()). SAGA_CHAPTER_III: GrantKeywordEffect(FLYING, FIRST_STRIKE, LIFELINK, TARGET, predicate) |
| 3-chapter Saga with graveyard targeting chapters + spell copy | `t/TheMirariConjecture.java` | SAGA_CHAPTER_I: ReturnCardFromGraveyardEffect(HAND, instant). SAGA_CHAPTER_II: ReturnCardFromGraveyardEffect(HAND, sorcery). Uses `canTargetGraveyard()` for graveyard target selection. SAGA_CHAPTER_III: GrantInstantSorceryCopyUntilEndOfTurnEffect |
