# CARD_PATTERN_INDEX

Purpose: quickly find a reference card for the pattern you're implementing. One or two examples per archetype. All paths relative to `cards/`.

## Lands

| Pattern | Reference | Notes |
|---------|-----------|-------|
| Basic land | `f/Forest.java` | `addEffect(ON_TAP, AwardManaEffect(color))` |
| Pain land | `s/SulfurousSprings.java` | 3 activated abilities: colorless + 2x colored with DealDamageToController |
| Creature land (manland) | `t/TreetopVillage.java` | `setEntersTapped` + ON_TAP mana + AnimateLandEffect ability |
| Creature land + sub-ability | `s/SpawningPool.java` | manland + regenerate with `ONLY_WHILE_CREATURE` restriction |
| Fast land | `b/BlackcleaveCliffs.java` | STATIC EntersTappedUnlessFewLandsEffect(2) + 2 mana abilities |
| Utility land | `q/Quicksand.java` | mana ability + sacrifice-to-debuff ability |

## Spells

| Pattern | Reference | Notes |
|---------|-----------|-------|
| Targeted burn | `s/Shock.java` | SPELL DealDamageToAnyTargetEffect (targeting auto-derived) |
| X burn | `b/Blaze.java` | DealXDamageToAnyTargetEffect |
| Burn + life drain | `e/EssenceDrain.java` | DealDamageToAnyTargetAndGainLifeEffect |
| X drain all opponents | `e/Exsanguinate.java` | EachOpponentLosesXLifeAndControllerGainsLifeLostEffect — no target, X life loss + gain |
| Multi-target damage | `c/ConeOfFlame.java` | DealOrderedDamageToAnyTargetsEffect |
| Damage all creatures | `p/Pyroclasm.java` | MassDamageEffect |
| Damage filtered creatures + players | `h/Hurricane.java` | MassDamageEffect (X, filter, damagesPlayers) |
| Pump target | `g/GiantGrowth.java` | BoostTargetCreatureEffect |
| Pump all + keyword | `o/Overrun.java` | BoostAllOwnCreaturesEffect + GrantKeywordEffect |
| Targeted destroy | `t/Terror.java` | DestroyTargetPermanentEffect + target filter |
| Multi-effect removal | `c/Condemn.java` | PutTargetOnBottomOfLibrary + GainLifeEqualToTargetToughness |
| Metalcraft sacrifice instant | `d/DispenseJustice.java` | SacrificeAttackingCreaturesEffect(1, 2) + PlayerPredicateTargetFilter(ANY) — metalcraft checked at resolution |
| Destroy + cantrip | `s/Smash.java` | DestroyTargetPermanentEffect + DrawCardEffect |
| Board wipe | `w/WrathOfGod.java` | DestroyAllPermanentsEffect |
| Counter (any) | `c/Cancel.java` | CounterSpellEffect (spell targeting auto-derived) |
| Counter (filtered) | `r/RemoveSoul.java` | StackEntryPredicateTargetFilter + StackEntryTypeInPredicate |
| Counter + bonus | `d/Discombobulate.java` | Counter + ReorderTopCardsOfLibraryEffect |
| Bounce target | `u/Unsummon.java` | ReturnTargetPermanentToHandEffect |
| Bounce mass | `e/Evacuation.java` | ReturnCreaturesToOwnersHandEffect |
| Pure draw | `c/CounselOfTheSoratami.java` | DrawCardEffect |
| Draw + discard | `s/Sift.java` | DrawCardEffect + DiscardCardEffect |
| Library selection | `t/TellingTime.java` | LookAtTopCardsHandTopBottomEffect |
| Targeted discard | `d/Distress.java` | ChooseCardFromTargetHandToDiscardEffect |
| Tutor to hand | `d/DiabolicTutor.java` | SearchLibraryForCardToHandEffect |
| Tutor to battlefield | `r/RampantGrowth.java` | SearchLibraryForCardTypesToBattlefieldEffect |
| Graveyard return (to hand) | `r/Recollect.java` | ReturnCardFromGraveyardEffect(HAND, null, true) — any card, targets graveyard |
| Graveyard return (to battlefield) | `b/BeaconOfUnrest.java` | ReturnCardFromGraveyardEffect(BATTLEFIELD, CardAnyOfPredicate, ALL_GRAVEYARDS) |
| Prevent combat damage | `h/HolyDay.java` | PreventAllCombatDamageEffect |
| Steal creature (temp) | `t/Threaten.java` | GainControlOfTargetCreatureUntilEndOfTurn + haste + untap |
| Extra turn | `t/TimeStretch.java` | ExtraTurnEffect |
| Extra combat | `r/RelentlessAssault.java` | AdditionalCombatMainPhaseEffect |
| Mill | `t/Traumatize.java` | MillHalfLibraryEffect |
| Shuffle-back spell | `b/BeaconOfDestruction.java` | Effect + ShuffleIntoLibraryEffect |
| Bite (pump + bite) | `a/AssertPerfection.java` | BoostFirstTargetCreatureEffect + FirstTargetDealsPowerDamageToSecondTargetEffect, multi-target with per-position filters |

## Vanilla creatures (empty body, all from Scryfall)

Reference: `a/AirElemental.java` — no constructor code needed.

## Keyword creatures (keywords from Scryfall, empty body)

| Pattern | Reference | Notes |
|---------|-----------|-------|
| Infect creature | `b/BlackcleaveGoblin.java` | Haste + Infect auto-loaded from Scryfall. Infect deals damage as -1/-1 counters to creatures and poison counters to players. |
| Intimidate creature + activated ability | `g/GethLordOfTheVault.java` | Intimidate from Scryfall + X-cost graveyard-targeting activated ability. PutCardFromOpponentGraveyardOntoBattlefieldEffect(tapped=true) |

## ETB creatures

| Pattern | Reference | Notes |
|---------|-----------|-------|
| ETB gain life | `a/AngelOfMercy.java` | ON_ENTER_BATTLEFIELD GainLifeEffect |
| ETB draw | `k/KavuClimber.java` | ON_ENTER_BATTLEFIELD DrawCardEffect |
| ETB draw + downside | `p/PhyrexianRager.java` | Draw + LoseLifeEffect |
| ETB destroy (targeted) | `n/Nekrataal.java` | ON_ENTER_BATTLEFIELD DestroyTargetPermanentEffect (targeting auto-derived) |
| ETB may destroy (filtered) | `a/AcidWebSpider.java` | MayEffect(DestroyTargetPermanentEffect) + PermanentPredicateTargetFilter |
| ETB discard | `r/RavenousRats.java` | TargetPlayerDiscardsEffect |
| ETB search | `c/CivicWayfinder.java` | MayEffect(SearchLibraryForBasicLandToHandEffect) |
| ETB may return from GY | `g/Gravedigger.java` | MayEffect(ReturnCardFromGraveyardEffect(HAND, CardTypePredicate(CREATURE))) |
| ETB tokens + ability | `s/SiegeGangCommander.java` | CreateCreatureTokenEffect + activated sac ability |
| ETB copy | `c/Clone.java` | CopyPermanentOnEnterEffect |
| ETB choose color | `v/VoiceOfAll.java` | ProtectionFromChosenColorEffect |
| ETB choose name | `p/PithingNeedle.java` | ChooseCardNameOnEnterEffect + static lock |
| ETB control handoff | `s/SleeperAgent.java` | TargetPlayerGainsControlOfSourceCreatureEffect |
| ETB drawback (discard) | `h/HiddenHorror.java` | SacrificeUnlessDiscardCardTypeEffect |
| ETB drawback (bounce artifact) | `g/GlintHawk.java` | SacrificeUnlessReturnOwnPermanentTypeToHandEffect(ARTIFACT) — sacrifice unless return own artifact to hand |
| ETB -1/-1 counters + counter removal ability | `b/BurdenedStoneback.java` | PutCountersOnSourceEffect(-1,-1,2) + RemoveCounterFromSourceCost + GrantKeywordEffect |
| ETB -1/-1 counters + mass -1/-1 ability | `c/CarnifexDemon.java` | PutCountersOnSourceEffect(-1,-1,2) + RemoveCounterFromSourceCost + PutMinusOneMinusOneCounterOnEachOtherCreatureEffect |
| ETB metalcraft conditional drain | `b/BleakCovenVampires.java` | MetalcraftConditionalEffect(TargetPlayerLosesLifeAndControllerGainsLifeEffect) — intervening-if 3+ artifacts |
| ETB metalcraft conditional boost + haste | `b/BladeTribeBerserkers.java` | Two MetalcraftConditionalEffect wrappers: BoostSelfEffect(3,3) + GrantKeywordEffect(HASTE, SELF) — multiple wrapped effects on same slot |
| ETB each player poison | `i/IchorRats.java` | ON_ENTER_BATTLEFIELD GiveEachPlayerPoisonCountersEffect(1) — infect creature, each player gets a poison counter |

## Triggered creatures

| Pattern | Reference | Notes |
|---------|-----------|-------|
| On death | `b/BogardanFirefiend.java` | ON_DEATH DealDamageToTargetCreatureEffect |
| Upkeep sacrifice/discard | `r/RazormaneMasticore.java` | UPKEEP_TRIGGERED + DRAW_TRIGGERED |
| Upkeep bounce | `s/StampedingWildebeests.java` | BounceCreatureOnUpkeepEffect |
| Upkeep token | `v/VerdantForce.java` | EACH_UPKEEP_TRIGGERED CreateCreatureTokenEffect |
| Graveyard upkeep | `s/SqueeGoblinNabob.java` | GRAVEYARD_UPKEEP_TRIGGERED ReturnCardFromGraveyardEffect(HAND, CardIsSelfPredicate, returnAll=true) |
| Combat damage to player | `t/ThievingMagpie.java` | ON_COMBAT_DAMAGE_TO_PLAYER DrawCardEffect |
| On becomes blocked | `s/SylvanBasilisk.java` | ON_BECOMES_BLOCKED DestroyCreatureBlockingThisEffect |
| On block (mutual destroy) | `l/LoyalSentry.java` | ON_BLOCK DestroyBlockedCreatureAndSelfEffect |
| On block conditional boost | `e/EzurisArchers.java` | ON_BLOCK BoostSelfWhenBlockingKeywordEffect(FLYING, 3, 0) — conditional trigger checked at block time |
| On damaged creature dies | `s/SengirVampire.java` | ON_DAMAGED_CREATURE_DIES PutPlusOnePlusOneCounterOnSourceEffect |
| Other creature enters | `s/SoulWarden.java` | ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD GainLifeEffect |
| Ally creature dies | `g/GravePact.java` | ON_ALLY_CREATURE_DIES EachOpponentSacrificesCreatureEffect |
| Opponent draws | `u/UnderworldDreams.java` | ON_OPPONENT_DRAWS DealDamageToTargetPlayerEffect |
| Opponent discards | `m/Megrim.java` | ON_OPPONENT_DISCARDS DealDamageToDiscardingPlayerEffect |
| Spell cast trigger | `q/QuirionDryad.java` | ON_ANY_PLAYER_CASTS_SPELL +1/+1 counter |
| May gain life on spell cast | `a/AngelsFeather.java` | MayEffect(GainLifeOnSpellCastEffect(CardColorPredicate)) — also `g/GolemsHeart.java` with CardTypePredicate |
| May pay to deal damage on artifact cast | `e/Embersmith.java` | MayEffect(DealDamageToAnyTargetOnArtifactCastEffect) — pay mana + any-target trigger |
| Land tap trigger | `m/Manabarbs.java` | ON_ANY_PLAYER_TAPS_LAND DealDamageOnLandTapEffect |
| End step self-destruct | `s/SparkElemental.java` | END_STEP_TRIGGERED SacrificeSelfEffect |
| Discarded by opponent | `g/GuerrillaTactics.java` | ON_SELF_DISCARDED_BY_OPPONENT DealDamageToAnyTargetEffect |
| Imprint ETB + dies | `c/CloneShell.java` | Artifact Creature — ON_ENTER_BATTLEFIELD ImprintFromTopCardsEffect + ON_DEATH PutImprintedCreatureOntoBattlefieldEffect |
| Battle cry (attack boost others) | `a/AccorderPaladin.java` | ON_ATTACK BoostAllOwnCreaturesEffect(1, 0, PermanentAllOfPredicate(PermanentIsAttackingPredicate, PermanentNotPredicate(PermanentIsSourceCardPredicate))) |
| Damage-to-controller bounce | `d/DissipationField.java` | ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU ReturnDamageSourcePermanentToHandEffect — bounces any permanent that deals damage to controller |

## Static permanents

| Pattern | Reference | Notes |
|---------|-----------|-------|
| Subtype lord | `g/GoblinKing.java` | STATIC StaticBoostEffect with PermanentHasAnySubtypePredicate filter, ALL_CREATURES scope |
| Anthem (all own) | `g/GloriousAnthem.java` | STATIC StaticBoostEffect with OWN_CREATURES scope, no filter |
| Color boost/debuff | `a/AscendantEvincar.java` | STATIC StaticBoostEffect with PermanentColorInPredicate / PermanentNotPredicate filter |
| Color keyword lord | `b/BellowingTanglewurm.java` | STATIC GrantKeywordEffect with PermanentColorInPredicate filter, OWN_CREATURES scope |
| Keyword lord + spell trigger | `h/HandOfThePraetors.java` | STATIC StaticBoostEffect with PermanentHasKeywordPredicate(INFECT) filter, OWN_CREATURES scope + ON_ANY_PLAYER_CASTS_SPELL GiveTargetPlayerPoisonCountersEffect with CardAllOfPredicate(CREATURE, INFECT) |
| Shared-type pump | `c/CoatOfArms.java` | STATIC BoostBySharedCreatureTypeEffect |
| Can't block | `s/SpinelessThug.java` | STATIC CantBlockEffect |
| Must attack | `b/BloodrockCyclops.java` | STATIC MustAttackEffect |
| Evasion (blocked only by) | `e/ElvenRiders.java` | STATIC CanBeBlockedOnlyByFilterEffect |
| Block limit | `s/StalkingTiger.java` | STATIC CanBeBlockedByAtMostNCreaturesEffect |
| Unblockable | `p/PhantomWarrior.java` | STATIC CantBeBlockedEffect |
| Attack restriction | `s/SeaMonster.java` | STATIC CantAttackUnlessDefenderControlsMatchingPermanentEffect |
| Block restriction | `c/CloudElemental.java` | STATIC CanBlockOnlyIfAttackerMatchesPredicateEffect |
| Protection from colors | `p/PaladinEnVec.java` | STATIC ProtectionFromColorsEffect |
| Prevent all damage to self | `c/ChoMannoRevolutionary.java` | STATIC PreventAllDamageEffect |
| Can't lose game | `p/PlatinumAngel.java` | STATIC CantLoseGameEffect |
| Controller shroud | `t/TrueBeliever.java` | STATIC GrantControllerShroudEffect |
| Can't cast type | `s/SteelGolem.java` | STATIC CantCastSpellTypeEffect |
| Limit spells | `r/RuleOfLaw.java` | STATIC LimitSpellsPerTurnEffect |
| Tax attackers | `w/WindbornMuse.java` | STATIC RequirePaymentToAttackEffect |
| Tax opponent spells | `a/AuraOfSilence.java` | STATIC IncreaseOpponentCastCostEffect |
| Enters tapped | `r/RootMaze.java` | STATIC EnterPermanentsOfTypesTappedEffect |
| P/T = lands | `m/MolimoMaroSorcerer.java` | STATIC PowerToughnessEqualToControlledLandCountEffect |
| P/T = creatures | `s/ScionOfTheWild.java` | STATIC PowerToughnessEqualToControlledCreatureCountEffect |
| P/T = subtype | `n/Nightmare.java` | STATIC PowerToughnessEqualToControlledSubtypeCountEffect |
| P/T = GY creatures | `m/Mortivore.java` | STATIC PowerToughnessEqualToCreatureCardsInAllGraveyardsEffect |
| +1/+1 per same name | `r/RelentlessRats.java` | STATIC BoostByOtherCreaturesWithSameNameEffect |
| Cost reduction | `a/AvatarOfMight.java` | STATIC ReduceOwnCastCostIfOpponentControlsMoreCreaturesEffect |
| No max hand size | `s/Spellbook.java` | STATIC NoMaximumHandSizeEffect |
| Double damage | `f/FurnaceOfRath.java` | STATIC DoubleDamageEffect |
| Play lands from GY | `c/CrucibleOfWorlds.java` | STATIC PlayLandsFromGraveyardEffect |
| Draw replacement | `a/Abundance.java` | STATIC AbundanceDrawReplacementEffect |
| Metalcraft keyword | `a/AuriokEdgewright.java` | STATIC MetalcraftConditionalEffect(GrantKeywordEffect(DOUBLE_STRIKE, SELF)) |
| Metalcraft keyword + boost | `a/AuriokSunchaser.java` | STATIC MetalcraftConditionalEffect(GrantKeywordEffect) + MetalcraftConditionalEffect(StaticBoostEffect) |
| Metalcraft boost only | `c/CarapaceForger.java` | STATIC MetalcraftConditionalEffect(StaticBoostEffect(2, 2, SELF)) |
| Metalcraft burn spell | `g/GalvanicBlast.java` | SPELL MetalcraftReplacementEffect(DealDamageToAnyTargetEffect(2), DealDamageToAnyTargetEffect(4)) — picks base/upgrade at resolution |

## Auras

| Pattern | Reference | Notes |
|---------|-----------|-------|
| Lockdown (can't attack/block) | `p/Pacifism.java` | STATIC EnchantedCreatureCantAttackOrBlockEffect |
| Full lockdown (can't attack/block/activate) | `a/Arrest.java` | STATIC EnchantedCreatureCantAttackOrBlockEffect + EnchantedCreatureCantActivateAbilitiesEffect |
| Doesn't untap | `d/Dehydration.java` | STATIC EnchantedCreatureDoesntUntapEffect |
| Static boost | `h/HolyStrength.java` | STATIC BoostAttachedCreatureEffect |
| Boost + keyword | `s/SerrasEmbrace.java` | Boost + GrantKeywordEffect(ENCHANTED_CREATURE) |
| Boost per subtype | `b/BlanchwoodArmor.java` | STATIC BoostEnchantedCreaturePerControlledSubtypeEffect |
| Control enchanted | `p/Persuasion.java` | STATIC ControlEnchantedCreatureEffect |
| Grant activated ability | `a/ArcaneTeachings.java` | GrantActivatedAbilityEffect with GrantScope.ENCHANTED_CREATURE |
| Redirect damage to creature | `p/Pariah.java` | STATIC RedirectPlayerDamageToEnchantedCreatureEffect |
| Enchanted land mana | `o/Overgrowth.java` | ON_ANY_PLAYER_TAPS_LAND AddManaOnEnchantedLandTapEffect |
| Aura + self-bounce ability | `s/ShimmeringWings.java` | STATIC keyword + activated ReturnSelfToHandEffect |

## Artifacts

| Pattern | Reference | Notes |
|---------|-----------|-------|
| Targeted ETB + activated ability | `c/ContagionClasp.java` | ON_ENTER_BATTLEFIELD PutMinusOneMinusOneCounterOnTargetCreatureEffect + tap+mana ProliferateEffect activated ability |
| Sac creature for counter + sac self for draw | `c/CullingDais.java` | Two abilities: tap+SacrificeCreatureCost+PutChargeCounterOnSelfEffect, mana+SacrificeSelfCost+DrawCardsEqualToChargeCountersOnSourceEffect |
| Spellbomb (sac for effect + may-pay draw) | `f/FlightSpellbomb.java` | Tap+SacrificeSelfCost+GrantKeywordEffect(TARGET) ability + ON_DEATH MayPayManaEffect("{U}", DrawCardEffect(1)) — Spellbomb cycle pattern |
| Charge counter trigger + activated token | `g/GolemFoundry.java` | MayEffect(PutChargeCounterOnSelfOnArtifactCastEffect) on ON_ANY_PLAYER_CASTS_SPELL + activated RemoveChargeCountersFromSourceCost(3) + CreateCreatureTokenEffect |

## Equipment

| Pattern | Reference | Notes |
|---------|-----------|-------|
| Simple boost equip | `l/LeoninScimitar.java` | STATIC BoostAttachedCreatureEffect + EquipEffect ability |
| Boost + keywords equip | `l/LoxodonWarhammer.java` | Boost + GrantKeywordEffect(EQUIPPED_CREATURE) + equip |
| Living weapon equip | `s/Strandwalker.java` | ON_ENTER_BATTLEFIELD LivingWeaponEffect + STATIC BoostAttachedCreatureEffect + GrantKeywordEffect(EQUIPPED_CREATURE) + equip |
| Grant ability + doesn't untap equip | `h/HeavyArbalest.java` | STATIC EquippedCreatureDoesntUntapEffect + GrantActivatedAbilityEffect(EQUIPPED_CREATURE) + equip |

## Activated abilities

| Pattern | Reference | Notes |
|---------|-----------|-------|
| Tap to damage any | `p/ProdigalPyromancer.java` | `(true, null, DealDamageToAnyTargetEffect, true)` |
| Mana + tap to damage | `r/RodOfRuin.java` | `(true, "{3}", DealDamageToAnyTargetEffect, true)` |
| Damage + self-damage | `o/OrcishArtillery.java` | Two effects: damage target + DealDamageToControllerEffect |
| Pump self | `f/FurnaceWhelp.java` | `(false, "{R}", BoostSelfEffect, false)` |
| Pump target | `g/GhostWarden.java` | `(true, null, BoostTargetCreatureEffect, true)` |
| Pump target + filter | `h/HateWeaver.java` | With PermanentPredicateTargetFilter |
| Tap to tap target | `i/IcyManipulator.java` | TapTargetPermanentEffect + PermanentPredicateTargetFilter |
| Tap to draw | `a/ArcanisTheOmnipotent.java` | `(true, null, DrawCardEffect, false)` |
| Draw + discard | `m/MerfolkLooter.java` | DrawCardEffect + DiscardCardEffect |
| Grant keyword to target | `m/MightWeaver.java` | GrantKeywordEffect with color filter |
| Sacrifice self for effect | `b/BottleGnomes.java` | SacrificeSelfCost + effect |
| Sacrifice self + choose source prevention | `a/AuriokReplica.java` | SacrificeSelfCost + PreventAllDamageFromChosenSourceEffect (prompts permanent choice on resolution) |
| Sacrifice subtype for effect | `s/SiegeGangCommander.java` | SacrificeSubtypeCreatureCost + DealDamageToAnyTargetEffect |
| Sacrifice artifact for effect | `b/BarrageOgre.java` | SacrificeArtifactCost + DealDamageToAnyTargetEffect (tap + sac artifact) |
| Regenerate (self) | `d/DrudgeSkeletons.java` | `(false, "{B}", RegenerateEffect, false)` |
| Regenerate (target creature) | `a/Asceticism.java` | `RegenerateEffect(true)` + PermanentPredicateTargetFilter |
| Static effect grant (own creatures) | `a/Asceticism.java` | `GrantEffectEffect(CantBeTargetOfSpellsOrAbilitiesEffect, GrantScope.OWN_CREATURES)` |
| Create token | `d/DragonRoost.java` | CreateCreatureTokenEffect |
| Mill target | `m/Millstone.java` | `(true, "{2}", MillTargetPlayerEffect, true)` |
| Mana dork (tap for color) | `b/BirdsOfParadise.java` | `(true, null, AwardAnyColorManaEffect, false)` |
| Mana dork (ON_TAP) | `l/LlanowarElves.java` | addEffect(ON_TAP, AwardManaEffect) |
| Animate self | `c/ChimericStaff.java` | AnimateSelfEffect |
| Sorcery-speed ability | `t/ThrullSurgeon.java` | ActivationTimingRestriction.SORCERY_SPEED |
| Metalcraft exile-flicker | `a/ArgentSphinx.java` | ExileSelfAndReturnAtEndStepEffect + METALCRAFT restriction |
| Metalcraft tap ability | `v/VedalkenCertarch.java` | ActivationTimingRestriction.METALCRAFT + target filter |
| Power-gated tap ability | `b/BloodshotTrainee.java` | ActivationTimingRestriction.POWER_4_OR_GREATER + creature target filter |
| Loyalty (no target) | `a/AjaniOutlandChaperone.java` | `(+1, effects, false, description)` |
| Loyalty (with target filter) | `a/AjaniOutlandChaperone.java` | `(-2, effects, true, description, filter)` |
