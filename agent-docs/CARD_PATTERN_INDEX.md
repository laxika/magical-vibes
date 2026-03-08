# CARD_PATTERN_INDEX

Purpose: quickly find a reference card for the pattern you're implementing. One or two examples per archetype. All paths relative to `cards/`.

## Lands

| Pattern | Reference | Notes |
|---------|-----------|-------|
| Basic land | `f/Forest.java` | `addEffect(ON_TAP, AwardManaEffect(color))` |
| Pain land | `s/SulfurousSprings.java` | 3 activated abilities: colorless + 2x colored with DealDamageToController |
| Creature land (manland) | `t/TreetopVillage.java` | `setEntersTapped` + ON_TAP mana + AnimateLandEffect ability |
| Creature land (artifact) | `i/InkmothNexus.java` | manland that becomes artifact creature (uses 6-arg AnimateLandEffect with grantedCardTypes) |
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
| Modal spell (choose one) | `s/Slagstorm.java` | ChooseOneEffect wrapping multiple CardEffects (e.g. MassDamageEffect + DealDamageToEachPlayerEffect). Mode chosen at cast time via `xValue` parameter (0-based index). Test with `castSorcery(player, idx, modeIndex)` |
| Modal spell (choose one or both) | `r/RememberTheFallen.java` | ChooseOneEffect with 3 options: mode 0 (creature), mode 1 (artifact), mode 2 (both). Each wraps ReturnTargetCardsFromGraveyardToHandEffect with appropriate filter. "Both" mode uses CardAnyOfPredicate with maxTargets=2. SpellCastingService unwraps ChooseOneEffect before graveyard targeting detection |
| Damage filtered creatures + players | `h/Hurricane.java` | MassDamageEffect (X, filter, damagesPlayers) |
| Pump target | `g/GiantGrowth.java` | BoostTargetCreatureEffect |
| X pump target | `u/UntamedMight.java` | BoostTargetCreatureXEffect |
| Pump all + keyword | `o/Overrun.java` | BoostAllOwnCreaturesEffect + GrantKeywordEffect |
| Grant triggered bounce | `a/ArmWithAether.java` | GrantDamageToOpponentCreatureBounceUntilEndOfTurnEffect — grants "damage to opponent, may bounce creature" to all controlled creatures until end of turn |
| Protection choice (color or artifacts) | `a/ApostlesBlessing.java` | GrantProtectionChoiceUntilEndOfTurnEffect(true) + ControlledPermanentPredicateTargetFilter(AnyOf(artifact, creature)). Phyrexian mana. On resolution, player chooses color or artifacts → protection until end of turn |
| Pump attacking + keyword | `r/RallyTheForces.java` | BoostAllCreaturesEffect(filter) + GrantKeywordEffect(ALL_CREATURES, filter) with PermanentIsAttackingPredicate |
| Targeted destroy | `t/Terror.java` | DestroyTargetPermanentEffect + target filter |
| Multi-effect removal | `c/Condemn.java` | PutTargetOnBottomOfLibrary + GainLifeEqualToTargetToughness |
| Put on top of library | `b/BanishmentDecree.java` | PutTargetOnTopOfLibraryEffect + PermanentAnyOfPredicate filter (artifact/creature/enchantment) |
| Metalcraft sacrifice instant | `d/DispenseJustice.java` | SacrificeAttackingCreaturesEffect(1, 2) + PlayerPredicateTargetFilter(ANY) — metalcraft checked at resolution |
| Destroy + cantrip | `s/Smash.java` | DestroyTargetPermanentEffect + DrawCardEffect |
| Destroy + life gain by mana value | `d/DivineOffering.java` | DestroyTargetPermanentAndGainLifeEqualToManaValueEffect + artifact filter |
| Destroy + controller life loss | `g/GlissasScorn.java` | DestroyTargetPermanentAndControllerLosesLifeEffect(1) + artifact filter |
| Board wipe | `w/WrathOfGod.java` | DestroyAllPermanentsEffect |
| Board wipe + opponent library search to graveyard | `l/LifesFinale.java` | DestroyAllPermanentsEffect + SearchTargetLibraryForCardsToGraveyardEffect(3, CREATURE) + PlayerPredicateTargetFilter(OPPONENT) |
| Counter (any) | `c/Cancel.java` | CounterSpellEffect (spell targeting auto-derived) |
| Counter (filtered by type) | `r/RemoveSoul.java` | StackEntryPredicateTargetFilter + StackEntryTypeInPredicate |
| Counter (filtered by mana value) | `m/MentalMisstep.java` | StackEntryPredicateTargetFilter + StackEntryManaValuePredicate. Phyrexian mana cost |
| Counter + bonus | `d/Discombobulate.java` | Counter + ReorderTopCardsOfLibraryEffect |
| Counter (filtered) + life loss | `p/PsychicBarrier.java` | TargetSpellControllerLosesLifeEffect(1) + CounterSpellEffect + creature-spell filter. Life loss placed before counter so target is still on stack |
| Counter + metalcraft cost reduction | `s/StoicRebuttal.java` | CounterSpellEffect + ReduceOwnCastCostIfMetalcraftEffect(1) — costs {1} less with 3+ artifacts |
| Counter (conditional, poisoned) | `c/CorruptedResolve.java` | CounterSpellIfControllerPoisonedEffect — counters only if target spell's controller is poisoned |
| Bounce target | `u/Unsummon.java` | ReturnTargetPermanentToHandEffect |
| Bounce mass | `e/Evacuation.java` | ReturnCreaturesToOwnersHandEffect |
| Pure draw | `c/CounselOfTheSoratami.java` | DrawCardEffect |
| Draw + discard | `s/Sift.java` | DrawCardEffect + DiscardCardEffect |
| Library selection | `t/TellingTime.java` | LookAtTopCardsHandTopBottomEffect |
| Library match-permanent-to-battlefield | `m/MitoticManipulation.java` | LookAtTopCardsPutMatchingPermanentNameOnBattlefieldEffect |
| Targeted discard | `d/Distress.java` | ChooseCardFromTargetHandToDiscardEffect |
| Exile by name (multi-zone) | `m/Memoricide.java` | ChooseCardNameAndExileFromZonesEffect(excludedTypes) — choose nonland name, exile from hand+graveyard+library, shuffle |
| Tutor to hand | `d/DiabolicTutor.java` | SearchLibraryForCardToHandEffect |
| Tutor + exile + opponent choice | `d/DistantMemories.java` | DistantMemoriesEffect — search, exile, opponent may let you have it or draw 3 |
| Tutor to battlefield | `r/RampantGrowth.java` | SearchLibraryForCardTypesToBattlefieldEffect |
| Graveyard return (to hand) | `r/Recollect.java` | ReturnCardFromGraveyardEffect(HAND, null, true) — any card, targets graveyard |
| Graveyard return (multi-target to hand) | `m/MorbidPlunder.java` | ReturnTargetCardsFromGraveyardToHandEffect(CardTypePredicate(CREATURE), 2) — up to N target cards to hand |
| Graveyard return (to battlefield) | `b/BeaconOfUnrest.java` | ReturnCardFromGraveyardEffect(BATTLEFIELD, CardAnyOfPredicate, ALL_GRAVEYARDS) |
| Graveyard to top of owner's library | `n/NoxiousRevival.java` | ReturnCardFromGraveyardEffect(TOP_OF_OWNERS_LIBRARY, null, ALL_GRAVEYARDS, true, ...) — any card from any graveyard on top of owner's library. Phyrexian mana |
| Graveyard to top of library + draw | `f/FranticSalvage.java` | PutTargetCardsFromGraveyardOnTopOfLibraryEffect(CardTypePredicate(ARTIFACT)) + DrawCardEffect — any number of target artifact cards, multi-graveyard targeting at cast time |
| Prevent combat damage | `h/HolyDay.java` | PreventAllCombatDamageEffect |
| Steal creature (temp) | `t/Threaten.java` | GainControlOfTargetCreatureUntilEndOfTurn + haste + untap |
| Steal artifact (temp) | `m/MetallicMastery.java` | GainControlOfTargetPermanentUntilEndOfTurn + untap + haste + PermanentIsArtifactPredicate filter |
| Extra turn | `t/TimeStretch.java` | ExtraTurnEffect |
| Extra combat | `r/RelentlessAssault.java` | AdditionalCombatMainPhaseEffect |
| Mill | `t/Traumatize.java` | MillHalfLibraryEffect |
| Shuffle-back spell | `b/BeaconOfDestruction.java` | Effect + ShuffleIntoLibraryEffect |
| X burn + exile-instead-of-die + shuffle | `r/RedSunsZenith.java` | DealXDamageToAnyTargetEffect(true) + ShuffleIntoLibraryEffect |
| X tokens + shuffle | `w/WhiteSunsZenith.java` | CreateXCreatureTokenEffect + ShuffleIntoLibraryEffect |
| Bite (pump + bite) | `a/AssertPerfection.java` | BoostFirstTargetCreatureEffect + FirstTargetDealsPowerDamageToSecondTargetEffect, multi-target with per-position filters |
| Pump + debuff (two targets) | `l/LeechingBite.java` | BoostFirstTargetCreatureEffect + BoostSecondTargetCreatureEffect, multi-target with creature filters |
| Damage creature + destroy equipment | `t/TurnToSlag.java` | DestroyEquipmentAttachedToTargetCreatureEffect + DealDamageToTargetCreatureEffect — equipment destruction placed before damage (engine destroys creatures immediately on lethal) |
| Sacrifice artifact spell cost + tokens | `k/KuldothaRebirth.java` | SacrificeArtifactCost + CreateCreatureTokenEffect — sacrifice artifact as additional spell cost |
| Sacrifice permanent spell cost + burn | `a/Artillerize.java` | SacrificePermanentCost(PermanentAnyOfPredicate) + DealDamageToAnyTargetEffect — sacrifice artifact or creature as additional spell cost |
| Sacrifice creature spell cost + power-based mass debuff | `i/IchorExplosion.java` | SacrificeCreatureCost(false, true) + BoostAllCreaturesXEffect(-1, -1) — sacrifice creature, all creatures get -X/-X where X = sacrificed creature's power |
| Graveyard-count damage | `s/ScrapyardSalvo.java` | DealDamageToTargetPlayerEqualToCardTypeCountInGraveyardEffect(ARTIFACT) — damage to target player equal to artifact cards in graveyard |

## Vanilla creatures (empty body, all from Scryfall)

Reference: `a/AirElemental.java` — no constructor code needed.

## Keyword creatures (keywords from Scryfall, empty body)

| Pattern | Reference | Notes |
|---------|-----------|-------|
| Infect creature | `b/BlackcleaveGoblin.java` | Haste + Infect auto-loaded from Scryfall. Infect deals damage as -1/-1 counters to creatures and poison counters to players. |
| Infect + damage replacement | `p/PhyrexianHydra.java` | Infect from Scryfall + STATIC PreventDamageAndAddMinusCountersEffect. Prevents all damage to self and puts -1/-1 counters instead. |
| Intimidate creature + activated ability | `g/GethLordOfTheVault.java` | Intimidate from Scryfall + X-cost graveyard-targeting activated ability. PutCardFromOpponentGraveyardOntoBattlefieldEffect(tapped=true) |
| Keyword creature + shuffle-into-library replacement | `b/BlightsteelColossus.java` | Keywords (infect, trample, indestructible) auto-loaded from Scryfall + `setShufflesIntoLibraryFromGraveyard(true)`. Replacement effect: when put into graveyard from anywhere, shuffled into owner's library instead. Also used by `l/LegacyWeapon.java`. |
| Can't be countered + keyword + ability | `t/ThrunTheLastTroll.java` | `setCantBeCountered(true)` + hexproof from Scryfall + `{1}{G}` RegenerateEffect activated ability. Intrinsic uncounterable checked by `GameQueryService.isUncounterable()` via `Card.isCantBeCountered()`. |

## ETB creatures

| Pattern | Reference | Notes |
|---------|-----------|-------|
| ETB gain life | `a/AngelOfMercy.java` | ON_ENTER_BATTLEFIELD GainLifeEffect |
| ETB draw | `k/KavuClimber.java` | ON_ENTER_BATTLEFIELD DrawCardEffect |
| ETB draw + downside | `p/PhyrexianRager.java` | Draw + LoseLifeEffect |
| ETB destroy (targeted) | `n/Nekrataal.java` | ON_ENTER_BATTLEFIELD DestroyTargetPermanentEffect (targeting auto-derived) |
| ETB may destroy (filtered) | `a/AcidWebSpider.java` | MayEffect(DestroyTargetPermanentEffect) + PermanentPredicateTargetFilter |
| ETB may exile until leaves (O-ring) | `l/LeoninRelicWarder.java` | MayEffect(ExileTargetPermanentUntilSourceLeavesEffect) + PermanentPredicateTargetFilter(AnyOf(artifact, enchantment)). Exiled card returns when source leaves battlefield |
| ETB discard | `r/RavenousRats.java` | TargetPlayerDiscardsEffect |
| ETB search | `c/CivicWayfinder.java` | MayEffect(SearchLibraryForBasicLandToHandEffect) |
| ETB search (type + min MV) | `t/TreasureMage.java` | MayEffect(SearchLibraryForCardTypesToHandEffect(ARTIFACT, 6, MAX_VALUE)) — artifact with MV 6+ |
| ETB may return from GY | `g/Gravedigger.java` | MayEffect(ReturnCardFromGraveyardEffect(HAND, CardTypePredicate(CREATURE))) |
| ETB cast from opponent's GY | `c/ChancellorOfTheSpires.java` | CastTargetInstantOrSorceryFromGraveyardEffect(OPPONENT_GRAVEYARD, true) — targets instant/sorcery in opponent's graveyard, may cast without paying. Also has ON_OPENING_HAND_REVEAL MayEffect(EachOpponentMillsEffect(7)) |
| ETB tokens + ability | `s/SiegeGangCommander.java` | CreateCreatureTokenEffect + activated sac ability |
| ETB copy | `c/Clone.java` | CopyPermanentOnEnterEffect |
| ETB copy with P/T override | `q/QuicksilverGargantuan.java` | CopyPermanentOnEnterEffect(filter, typeLabel, 7, 7) — "copy except it's 7/7" |
| ETB copy with type override | `p/PhyrexianMetamorph.java` | CopyPermanentOnEnterEffect(AnyOfPredicate, typeLabel, null, null, Set.of(ARTIFACT)) — "copy except it's also an artifact" |
| ETB choose color | `v/VoiceOfAll.java` | ProtectionFromChosenColorEffect |
| ETB choose name | `p/PithingNeedle.java` | ChooseCardNameOnEnterEffect + static lock |
| ETB choose nonland name | `p/PhyrexianRevoker.java` | ChooseCardNameOnEnterEffect(List.of(LAND)) + static lock — artifact creature variant |
| ETB control handoff | `s/SleeperAgent.java` | TargetPlayerGainsControlOfSourceCreatureEffect |
| ETB drawback (discard) | `h/HiddenHorror.java` | SacrificeUnlessDiscardCardTypeEffect |
| ETB drawback (bounce artifact) | `g/GlintHawk.java` | SacrificeUnlessReturnOwnPermanentTypeToHandEffect(ARTIFACT) — sacrifice unless return own artifact to hand |
| ETB -1/-1 counters + counter removal ability | `b/BurdenedStoneback.java` | PutCountersOnSourceEffect(-1,-1,2) + RemoveCounterFromSourceCost + GrantKeywordEffect |
| ETB -1/-1 counters + mass -1/-1 ability | `c/CarnifexDemon.java` | PutCountersOnSourceEffect(-1,-1,2) + RemoveCounterFromSourceCost + PutMinusOneMinusOneCounterOnEachOtherCreatureEffect |
| ETB -1/-1 counters + multi-counter removal + player draw | `e/EtchedMonstrosity.java` | PutCountersOnSourceEffect(-1,-1,5) + RemoveCounterFromSourceCost(5) + DrawCardForTargetPlayerEffect(3, false, true) with PlayerPredicateTargetFilter |
| ETB -1/-1 counters on target | `s/Skinrender.java` | PutMinusOneMinusOneCounterOnTargetCreatureEffect(3) + PermanentIsCreaturePredicate filter |
| ETB metalcraft conditional drain | `b/BleakCovenVampires.java` | MetalcraftConditionalEffect(TargetPlayerLosesLifeAndControllerGainsLifeEffect) — intervening-if 3+ artifacts |
| ETB metalcraft conditional boost + haste | `b/BladeTribeBerserkers.java` | Two MetalcraftConditionalEffect wrappers: BoostSelfEffect(3,3) + GrantKeywordEffect(HASTE, SELF) — multiple wrapped effects on same slot |
| ETB each player poison | `i/IchorRats.java` | ON_ENTER_BATTLEFIELD GiveEachPlayerPoisonCountersEffect(1) — infect creature, each player gets a poison counter |
| ETB may-sacrifice-artifact-divided-damage | `k/KuldothaFlamefiend.java` | MayEffect(SacrificeArtifactThenDealDividedDamageEffect(4)) — may sacrifice artifact, deal N divided damage. Damage assignments via `pendingETBDamageAssignments`, artifact choice via PermanentChoiceContext.SacrificeArtifactForDividedDamage |
| ETB modal (choose one, no target) | `b/BrutalizerExarch.java` | ChooseOneEffect on ON_ENTER_BATTLEFIELD with one non-targeting mode (library search) and one targeting mode. Card-level setTargetFilter for the targeting mode. Mode chosen at cast time via xValue |
| ETB modal (choose one, per-mode targeting) | `d/DeceiverExarch.java` | ChooseOneEffect on ON_ENTER_BATTLEFIELD with per-mode TargetFilter on each ChooseOneOption. Mode 0: UntapTargetPermanentEffect + PermanentControlledBySourceControllerPredicate. Mode 1: TapTargetPermanentEffect + PermanentNotPredicate(PermanentControlledBySourceControllerPredicate). Flash creature |

## Triggered creatures

| Pattern | Reference | Notes |
|---------|-----------|-------|
| On death | `b/BogardanFirefiend.java` | ON_DEATH DealDamageToTargetCreatureEffect |
| Upkeep sacrifice/discard | `r/RazormaneMasticore.java` | UPKEEP_TRIGGERED + DRAW_TRIGGERED |
| Exile-from-graveyard cost + damage | `m/MoltenTailMasticore.java` | ExileCardFromGraveyardCost(CREATURE) + DealDamageToAnyTargetEffect + RegenerateEffect |
| Upkeep bounce | `s/StampedingWildebeests.java` | BounceCreatureOnUpkeepEffect |
| Upkeep token | `v/VerdantForce.java` | EACH_UPKEEP_TRIGGERED CreateCreatureTokenEffect |
| Upkeep conditional self-bounce + tokens | `t/ThopterAssembly.java` | UPKEEP_TRIGGERED NoOtherSubtypeConditionalEffect(THOPTER, ReturnSelfToHandAndCreateTokensEffect) — intervening-if "no other Thopters", returns self + creates 5 tokens |
| Upkeep token per equipment | `k/KembaKhaRegent.java` | UPKEEP_TRIGGERED CreateTokenPerEquipmentOnSourceEffect — tokens equal to attached Equipment |
| Upkeep may target artifact charge | `v/VedalkenInfuser.java` | UPKEEP_TRIGGERED MayEffect(PutChargeCounterOnTargetPermanentEffect) + PermanentPredicateTargetFilter(PermanentIsArtifactPredicate) — may put charge counter on target artifact |
| Graveyard upkeep | `s/SqueeGoblinNabob.java` | GRAVEYARD_UPKEEP_TRIGGERED ReturnCardFromGraveyardEffect(HAND, CardIsSelfPredicate, returnAll=true) |
| Graveyard metalcraft pay-to-return | `k/KuldothaPhoenix.java` | GRAVEYARD_UPKEEP_TRIGGERED MetalcraftConditionalEffect(MayPayManaEffect("{4}", ReturnCardFromGraveyardEffect(BATTLEFIELD, CardIsSelfPredicate))) — metalcraft checked at trigger time, mana paid at resolution |
| Combat damage to player | `t/ThievingMagpie.java` | ON_COMBAT_DAMAGE_TO_PLAYER DrawCardEffect |
| Combat damage awakening | `l/LiegeOfTheTangle.java` | ON_COMBAT_DAMAGE_TO_PLAYER PutAwakeningCountersOnTargetLandsEffect — multi-permanent choice on controller's lands, permanent 8/8 animation via awakening counters |
| Combat damage may-sacrifice-draw | `i/ImpalerShrike.java` | ON_COMBAT_DAMAGE_TO_PLAYER `MayEffect(SacrificeSelfAndDrawCardsEffect(3))` — may sacrifice self, if you do draw N cards |
| Combat damage may-sacrifice-destroy | `b/BlindZealot.java` | ON_COMBAT_DAMAGE_TO_PLAYER `MayEffect(SacrificeSelfToDestroyCreatureDamagedPlayerControlsEffect)` — MayEffect wraps inner sacrifice+destroy. CombatService queues as PendingMayAbility with context; after acceptance, inner effect presents multi-permanent choice for target creature |
| On becomes blocked | `s/SylvanBasilisk.java` | ON_BECOMES_BLOCKED DestroyCreatureBlockingThisEffect |
| On block (mutual destroy) | `l/LoyalSentry.java` | ON_BLOCK DestroyBlockedCreatureAndSelfEffect |
| On block conditional boost | `e/EzurisArchers.java` | ON_BLOCK BoostSelfWhenBlockingKeywordEffect(FLYING, 3, 0) — conditional trigger checked at block time |
| On damaged creature dies | `s/SengirVampire.java` | ON_DAMAGED_CREATURE_DIES PutPlusOnePlusOneCounterOnSourceEffect |
| On dealt damage to self | `n/NestedGhoul.java` | ON_DEALT_DAMAGE CreateCreatureTokenEffect — fires per source, both combat and non-combat |
| On dealt damage: source controller poison | `r/ReaperOfSheoldred.java` | ON_DEALT_DAMAGE DamageSourceControllerGetsPoisonCounterEffect(null) marker — UUID filled at trigger time, gives source's controller 1 poison counter |
| On dealt damage: source controller sacrifices | `p/PhyrexianObliterator.java` | ON_DEALT_DAMAGE DamageSourceControllerSacrificesPermanentsEffect(0,null) marker — count+playerId filled at trigger time, multi-permanent choice for sacrifice |
| Other creature enters | `s/SoulWarden.java` | ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD GainLifeEffect |
| Ally creature dies | `g/GravePact.java` | ON_ALLY_CREATURE_DIES EachOpponentSacrificesCreatureEffect |
| Opponent creature dies | `g/GlissaTheTraitor.java` | ON_OPPONENT_CREATURE_DIES MayEffect(ReturnCardFromGraveyardEffect(HAND, CardTypePredicate(ARTIFACT))) — includes tokens |
| Any artifact goes to graveyard from battlefield | `m/MolderBeast.java` | ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD BoostSelfEffect |
| Any artifact goes to graveyard — damage controller | `m/MagneticMine.java` | ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD DealDamageToTriggeringPermanentControllerEffect(2) — target pre-set to artifact's controller at trigger time |
| Artifact put into opponent's graveyard from battlefield | `v/ViridianRevel.java` | ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD MayEffect(DrawCardEffect) |
| Opponent draws | `u/UnderworldDreams.java` | ON_OPPONENT_DRAWS DealDamageToTargetPlayerEffect |
| Opponent loses life — mill | `m/Mindcrank.java` | ON_OPPONENT_LOSES_LIFE MillOpponentOnLifeLossEffect — whenever opponent loses life, mills that many cards. Trigger fires from damage (spell + combat) and life loss effects |
| Opponent discards | `m/Megrim.java` | ON_OPPONENT_DISCARDS DealDamageToDiscardingPlayerEffect |
| Spell cast trigger | `q/QuirionDryad.java` | ON_ANY_PLAYER_CASTS_SPELL +1/+1 counter |
| Knowledge Pool-style cast intercept | `k/KnowledgePool.java` | ON_ENTER_BATTLEFIELD EachPlayerExilesTopCardsToSourceEffect(3) + ON_ANY_PLAYER_CASTS_SPELL KnowledgePoolCastTriggerEffect — exiles cast-from-hand spells, lets caster pick a nonland card from pool to cast free. Uses per-permanent exile tracking (permanentExiledCards) and KNOWLEDGE_POOL_CAST_CHOICE awaiting input |
| May gain life on spell cast | `a/AngelsFeather.java` | MayEffect(GainLifeOnSpellCastEffect(CardColorPredicate)) — also `g/GolemsHeart.java` with CardTypePredicate |
| May pay to deal damage on artifact cast | `e/Embersmith.java` | MayEffect(DealDamageToAnyTargetOnArtifactCastEffect) — pay mana + any-target trigger |
| May pay to create token on artifact cast | `m/Myrsmith.java` | MayEffect(CreateTokenOnOwnSpellCastWithCostEffect) — pay mana + create token trigger |
| May loot on spell cast | `r/Riddlesmith.java` | MayEffect(DrawAndDiscardOnOwnSpellCastEffect(CardTypePredicate)) — may draw+discard on matching spell cast |
| Opponent spell punisher (discard or life) | `p/PainfulQuandary.java` | ON_OPPONENT_CASTS_SPELL LoseLifeUnlessDiscardEffect(5) — opponent chooses discard or lose life |
| Opponent spell tax (counter unless pays) | `c/ChancellorOfTheAnnex.java` | ON_OPPONENT_CASTS_SPELL CounterUnlessPaysEffect(1) — auto-targets the triggering spell. Also has ON_OPENING_HAND_REVEAL MayEffect(RegisterDelayedCounterTriggerEffect(1)) for Chancellor-style delayed counter trigger on first opponent spell |
| Opponent creature spell punisher (pay or life) | `i/IsolationCell.java` | ON_OPPONENT_CASTS_SPELL LoseLifeUnlessPaysEffect(2, 2, CardTypePredicate(CREATURE)) — filtered to creature spells only, pay mana or lose life |
| Opponent land enters (punisher) | `t/TunnelIgnus.java` | ON_OPPONENT_LAND_ENTERS_BATTLEFIELD PermanentEnteredThisTurnConditionalEffect(DealDamageToTargetPlayerEffect(3), CardTypePredicate(LAND), 2) — conditional wrapper checks 2+ lands entered this turn |
| Imprint ETB exile land + opponent land name match | `i/InvaderParasite.java` | ON_ENTER_BATTLEFIELD ExileTargetPermanentAndImprintEffect + ON_OPPONENT_LAND_ENTERS_BATTLEFIELD ImprintedCardNameMatchesEnteringPermanentConditionalEffect(DealDamageToTargetPlayerEffect(2)) — exile target land on ETB, deal 2 damage when opponent plays land with same name |
| Land tap trigger | `m/Manabarbs.java` | ON_ANY_PLAYER_TAPS_LAND DealDamageOnLandTapEffect |
| End step self-destruct | `s/SparkElemental.java` | END_STEP_TRIGGERED SacrificeSelfEffect |
| Controller end step draw | `j/JinGitaxiasCoreAugur.java` | CONTROLLER_END_STEP_TRIGGERED DrawCardEffect(7) — "your end step" trigger (only fires on controller's turn) + STATIC ReduceOpponentMaxHandSizeEffect(7) |
| Discarded by opponent | `g/GuerrillaTactics.java` | ON_SELF_DISCARDED_BY_OPPONENT DealDamageToAnyTargetEffect |
| Imprint ETB + dies | `c/CloneShell.java` | Artifact Creature — ON_ENTER_BATTLEFIELD ImprintFromTopCardsEffect + ON_DEATH PutImprintedCreatureOntoBattlefieldEffect |
| Equipment enters may-draw + metalcraft equip {0} | `p/PuresteelPaladin.java` | ON_ALLY_EQUIPMENT_ENTERS_BATTLEFIELD MayEffect(DrawCardEffect) + STATIC MetalcraftConditionalEffect(GrantActivatedAbilityEffect(EquipActivatedAbility("{0}"), OWN_PERMANENTS, PermanentHasSubtypePredicate(EQUIPMENT))) |
| Battle cry (attack boost others) | `a/AccorderPaladin.java` | Engine-handled via `BATTLE_CRY` keyword — CombatService auto-generates BoostAllOwnCreaturesEffect(1, 0, attacking + not-self filter) trigger for any creature with the keyword. Card class is empty-body. |
| Battle cry + attack can't-block | `h/HeroOfOxidRidge.java` | ON_ATTACK CantBlockThisTurnEffect(PermanentPowerAtMostPredicate(1)) — battle cry keyword auto-handled, plus predicate-filtered mass can't-block on attack |
| Damage-to-controller bounce | `d/DissipationField.java` | ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU ReturnDamageSourcePermanentToHandEffect — bounces any permanent that deals damage to controller |
| Damage-to-controller steal | `c/ContestedWarZone.java` | ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU DamageSourceControllerGainsControlOfThisPermanentEffect(true, true) — combat creature damage causes opponent to gain control of this land |
| Upkeep become-copy | `c/Cryptoplasm.java` | UPKEEP_TRIGGERED BecomeCopyOfTargetCreatureEffect — mandatory target at trigger time (CR 603.3d), may choice at resolution. Uses pendingUpkeepCopyTargets queue for target selection, then CopyResolutionService queues pendingMayAbility for the may choice |

## Static permanents

| Pattern | Reference | Notes |
|---------|-----------|-------|
| Subtype lord | `g/GoblinKing.java` | STATIC StaticBoostEffect with PermanentHasAnySubtypePredicate filter, ALL_CREATURES scope |
| Anthem (all own) | `g/GloriousAnthem.java` | STATIC StaticBoostEffect with OWN_CREATURES scope, no filter |
| Color boost/debuff | `a/AscendantEvincar.java` | STATIC StaticBoostEffect with PermanentColorInPredicate / PermanentNotPredicate filter |
| Own boost + opponent debuff | `e/EleshNornGrandCenobite.java` | STATIC StaticBoostEffect(2, 2, OWN_CREATURES) + StaticBoostEffect(-2, -2, OPPONENT_CREATURES) |
| Color keyword lord | `b/BellowingTanglewurm.java` | STATIC GrantKeywordEffect with PermanentColorInPredicate filter, OWN_CREATURES scope |
| Keyword lord + spell trigger | `h/HandOfThePraetors.java` | STATIC StaticBoostEffect with PermanentHasKeywordPredicate(INFECT) filter, OWN_CREATURES scope + ON_ANY_PLAYER_CASTS_SPELL GiveTargetPlayerPoisonCountersEffect with CardAllOfPredicate(CREATURE, INFECT) |
| Shared-type pump | `c/CoatOfArms.java` | STATIC BoostBySharedCreatureTypeEffect |
| Can't block | `s/SpinelessThug.java` | STATIC CantBlockEffect |
| Must attack | `b/BloodrockCyclops.java` | STATIC MustAttackEffect |
| Evasion (blocked only by) | `e/ElvenRiders.java` | STATIC CanBeBlockedOnlyByFilterEffect |
| Block limit | `s/StalkingTiger.java` | STATIC CanBeBlockedByAtMostNCreaturesEffect |
| Unblockable | `p/PhantomWarrior.java` | STATIC CantBeBlockedEffect |
| Conditional unblockable | `s/ScrapdiverSerpent.java` | STATIC CantBeBlockedIfDefenderControlsMatchingPermanentEffect |
| Attack restriction | `s/SeaMonster.java` | STATIC CantAttackUnlessDefenderControlsMatchingPermanentEffect |
| Block restriction | `c/CloudElemental.java` | STATIC CanBlockOnlyIfAttackerMatchesPredicateEffect |
| Protection from colors | `p/PaladinEnVec.java` | STATIC ProtectionFromColorsEffect |
| Prevent all damage to self | `c/ChoMannoRevolutionary.java` | STATIC PreventAllDamageEffect |
| Can't lose game | `p/PlatinumAngel.java` | STATIC CantLoseGameEffect |
| Can't lose from life + damage as infect | `p/PhyrexianUnlife.java` | STATIC CantLoseGameFromLifeEffect + DamageDealtAsInfectBelowZeroLifeEffect |
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
| P/T = hand size + draw trigger | `p/PsychosisCrawler.java` | STATIC PowerToughnessEqualToCardsInHandEffect + ON_CONTROLLER_DRAWS EachOpponentLosesLifeEffect |
| Gain GY creature abilities | `n/NecroticOoze.java` | STATIC GainActivatedAbilitiesOfCreatureCardsInAllGraveyardsEffect — selfOnly, gains all activated abilities of all creature cards in all graveyards |
| +1/+1 per same name | `r/RelentlessRats.java` | STATIC BoostByOtherCreaturesWithSameNameEffect |
| Cost reduction | `a/AvatarOfMight.java` | STATIC ReduceOwnCastCostIfOpponentControlsMoreCreaturesEffect |
| Creature mana only | `m/MyrSuperion.java` | setRequiresCreatureMana(true) — can only be cast with mana produced by creatures |
| No max hand size | `s/Spellbook.java` | STATIC NoMaximumHandSizeEffect |
| Double damage | `f/FurnaceOfRath.java` | STATIC DoubleDamageEffect |
| Play lands from GY | `c/CrucibleOfWorlds.java` | STATIC PlayLandsFromGraveyardEffect |
| Draw replacement | `a/Abundance.java` | STATIC AbundanceDrawReplacementEffect |
| Grant flash to spell type | `s/ShimmerMyr.java` | STATIC GrantFlashToCardTypeEffect(ARTIFACT) — controller may cast artifact spells as though they had flash |
| Metalcraft keyword | `a/AuriokEdgewright.java` | STATIC MetalcraftConditionalEffect(GrantKeywordEffect(DOUBLE_STRIKE, SELF)) |
| Metalcraft keyword + boost | `a/AuriokSunchaser.java` | STATIC MetalcraftConditionalEffect(GrantKeywordEffect) + MetalcraftConditionalEffect(StaticBoostEffect) |
| Metalcraft boost only | `c/CarapaceForger.java` | STATIC MetalcraftConditionalEffect(StaticBoostEffect(2, 2, SELF)) |
| Metalcraft boost + ignore defender | `s/SpireSerpent.java` | STATIC MetalcraftConditionalEffect(StaticBoostEffect) + MetalcraftConditionalEffect(CanAttackAsThoughNoDefenderEffect) |
| Metalcraft become creature | `r/RustedRelic.java` | STATIC MetalcraftConditionalEffect(AnimateSelfWithStatsEffect(5, 5, [GOLEM], [])) — noncreature becomes creature with fixed P/T and subtypes |
| Metalcraft burn spell | `g/GalvanicBlast.java` | SPELL MetalcraftReplacementEffect(DealDamageToAnyTargetEffect(2), DealDamageToAnyTargetEffect(4)) — picks base/upgrade at resolution |
| Metalcraft damage + can't block | `c/ConcussiveBolt.java` | SPELL DealDamageToTargetPlayerEffect(4) + MetalcraftConditionalEffect(TargetPlayerCreaturesCantBlockThisTurnEffect) — damage always, metalcraft adds mass can't-block on target player's creatures |

## Auras

| Pattern | Reference | Notes |
|---------|-----------|-------|
| Lockdown (can't attack/block) | `p/Pacifism.java` | STATIC EnchantedCreatureCantAttackOrBlockEffect |
| Lockdown (can't attack) + self-bounce | `f/ForcedWorship.java` | STATIC EnchantedCreatureCantAttackEffect + activated ReturnSelfToHandEffect |
| Full lockdown (can't attack/block/activate) | `a/Arrest.java` | STATIC EnchantedCreatureCantAttackOrBlockEffect + EnchantedCreatureCantActivateAbilitiesEffect |
| Doesn't untap | `d/Dehydration.java` | STATIC EnchantedCreatureDoesntUntapEffect |
| Static boost | `h/HolyStrength.java` | STATIC BoostAttachedCreatureEffect |
| Boost + keyword | `s/SerrasEmbrace.java` | Boost + GrantKeywordEffect(ENCHANTED_CREATURE) |
| Boost per subtype | `b/BlanchwoodArmor.java` | STATIC BoostEnchantedCreaturePerControlledSubtypeEffect |
| Control enchanted | `p/Persuasion.java` | STATIC ControlEnchantedCreatureEffect |
| Grant activated ability | `a/ArcaneTeachings.java` | GrantActivatedAbilityEffect with GrantScope.ENCHANTED_CREATURE |
| Redirect damage to creature | `p/Pariah.java` | STATIC RedirectPlayerDamageToEnchantedCreatureEffect |
| Enchanted land mana | `o/Overgrowth.java` | ON_ANY_PLAYER_TAPS_LAND AddManaOnEnchantedLandTapEffect |
| Enchanted land becomes basic type | `e/EvilPresence.java` | STATIC EnchantedPermanentBecomesTypeEffect(SWAMP) — land loses all land types/abilities, becomes the new basic land type |
| Aura + self-bounce ability | `s/ShimmeringWings.java` | STATIC keyword + activated ReturnSelfToHandEffect |
| Grant keyword + upkeep counter + death return | `g/GlisteningOil.java` | STATIC GrantKeywordEffect(INFECT, ENCHANTED_CREATURE) + UPKEEP_TRIGGERED PutMinusOneMinusOneCounterOnEnchantedCreatureEffect + ON_DEATH ReturnCardFromGraveyardEffect(HAND, CardIsSelfPredicate) |
| Doesn't untap + enchanted controller upkeep life loss | `n/NumbingDose.java` | STATIC AttachedCreatureDoesntUntapEffect + ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED EnchantedCreatureControllerLosesLifeEffect(1) — enchants artifact or creature, uses PermanentAnyOfPredicate target filter |

## Artifacts

| Pattern | Reference | Notes |
|---------|-----------|-------|
| Targeted ETB + activated ability | `c/ContagionClasp.java` | ON_ENTER_BATTLEFIELD PutMinusOneMinusOneCounterOnTargetCreatureEffect + tap+mana ProliferateEffect activated ability |
| Sac creature for counter + sac self for draw | `c/CullingDais.java` | Two abilities: tap+SacrificeCreatureCost+PutChargeCounterOnSelfEffect, mana+SacrificeSelfCost+DrawCardsEqualToChargeCountersOnSourceEffect |
| ETB + death draw (simple) | `i/IchorWellspring.java` | ON_ENTER_BATTLEFIELD DrawCardEffect + ON_DEATH DrawCardEffect — draws on ETB and when destroyed/sacrificed |
| Spellbomb (sac for effect + may-pay draw) | `f/FlightSpellbomb.java` | Tap+SacrificeSelfCost+GrantKeywordEffect(TARGET) ability + ON_DEATH MayPayManaEffect("{U}", DrawCardEffect(1)) — Spellbomb cycle pattern |
| Charge counter trigger + activated token | `g/GolemFoundry.java` | MayEffect(PutChargeCounterOnSelfOnArtifactCastEffect) on ON_ANY_PLAYER_CASTS_SPELL + activated RemoveChargeCountersFromSourceCost(3) + CreateCreatureTokenEffect |
| Enters with fixed charge counters + tap-remove ability | `n/NecrogenCenser.java` | EnterWithFixedChargeCountersEffect(2) + tap+RemoveChargeCountersFromSourceCost(1)+TargetPlayerLosesLifeAndControllerGainsLifeEffect |
| Dual sacrifice abilities (damage + counter removal) | `g/GremlinMine.java` | Two abilities: tap+{1}+SacrificeSelfCost+DealDamageToTargetCreatureEffect(4) with artifact creature filter + tap+{1}+SacrificeSelfCost+RemoveChargeCountersFromTargetPermanentEffect(4) with noncreature artifact filter |
| Type-changing tap ability | `l/LiquimetalCoating.java` | Tap: AddCardTypeToTargetPermanentEffect(ARTIFACT) — target permanent becomes artifact until end of turn |
| Hand-imprint + X-cost token | `p/PrototypePortal.java` | ON_ENTER_BATTLEFIELD MayEffect(ExileFromHandToImprintEffect(CardTypePredicate(ARTIFACT))) + activated {X}+tap CreateTokenCopyOfImprintedCardEffect(false, false) — imprint artifact from hand on ETB, pay X (imprinted card's mana cost) to create permanent token copy |
| Hand-imprint + cost reduction | `s/SemblanceAnvil.java` | ON_ENTER_BATTLEFIELD MayEffect(ExileFromHandToImprintEffect(CardNotPredicate(CardTypePredicate(LAND)))) + STATIC ReduceOwnCastCostForSharedCardTypeWithImprintEffect(2) — imprint nonland card from hand on ETB, spells sharing card type with imprinted card cost {2} less |
| Static + upkeep trigger | `v/VensersJournal.java` | STATIC NoMaximumHandSizeEffect + UPKEEP_TRIGGERED GainLifePerCardsInHandEffect — no max hand size + gain life equal to hand size each upkeep |
| Sacrifice creature + search by MV | `b/BirthingPod.java` | Tap+mana(Phyrexian)+SacrificeCreatureCost(true)+SearchLibraryForCreatureWithExactMVToBattlefieldEffect(1) — sorcery speed, sacrifice creature to tutor creature with MV+1 to battlefield. `trackSacrificedManaValue=true` stores sacrificed MV in xValue |
| Choose color + static boost + mana bonus | `c/CagedSun.java` | ON_ENTER_BATTLEFIELD ChooseColorOnEnterEffect + STATIC BoostCreaturesOfChosenColorEffect(1,1) + ON_ANY_PLAYER_TAPS_LAND AddExtraManaOfChosenColorOnLandTapEffect — choose color on enter, creatures of chosen color get +1/+1, lands tapped for chosen color add extra mana |
| Shrine (upkeep + spell trigger charge + sac-for-mana) | `s/ShrineOfBoundlessGrowth.java` | UPKEEP_TRIGGERED PutChargeCounterOnSelfEffect + ON_CONTROLLER_CASTS_SPELL SpellCastTriggerEffect(CardColorPredicate) + tap+SacrificeSelfCost+AddColorlessManaPerChargeCounterOnSourceEffect — charge counters on upkeep and colored spell cast, sacrifice for mana |

## Equipment

| Pattern | Reference | Notes |
|---------|-----------|-------|
| Simple boost equip | `l/LeoninScimitar.java` | STATIC BoostAttachedCreatureEffect + EquipActivatedAbility |
| Boost + keywords equip | `l/LoxodonWarhammer.java` | Boost + GrantKeywordEffect(EQUIPPED_CREATURE) + equip |
| Boost + can't block equip | `c/CopperCarapace.java` | STATIC BoostAttachedCreatureEffect + CantBlockEffect + equip |
| ETB attach + sacrifice equip | `p/PistonSledge.java` | ON_ENTER_BATTLEFIELD AttachSourceEquipmentToTargetCreatureEffect + STATIC BoostAttachedCreatureEffect + equip with SacrificeArtifactCost (no mana). Uses ControlledPermanentPredicateTargetFilter on card for ETB targeting |
| Living weapon equip | `s/Strandwalker.java` | ON_ENTER_BATTLEFIELD LivingWeaponEffect + STATIC BoostAttachedCreatureEffect + GrantKeywordEffect(EQUIPPED_CREATURE) + equip |
| Living weapon + per-subtype boost equip | `l/Lashwrithe.java` | ON_ENTER_BATTLEFIELD LivingWeaponEffect + STATIC BoostAttachedCreaturePerControlledSubtypeEffect(SWAMP, 1, 1) + Phyrexian mana equip |
| Boost + color/subtype + death trigger equip | `n/NimDeathmantle.java` | STATIC BoostAttachedCreatureEffect + GrantKeywordEffect(EQUIPPED_CREATURE) + GrantColorEffect(BLACK, EQUIPPED_CREATURE) + GrantSubtypeEffect(ZOMBIE, EQUIPPED_CREATURE) + ON_ANY_NONTOKEN_CREATURE_DIES MayPayManaEffect("{4}", ReturnDyingCreatureToBattlefieldAndAttachSourceEffect) + equip |
| Imprint + land-name boost equip | `s/StrataScythe.java` | ON_ENTER_BATTLEFIELD SearchLibraryForCardTypeToExileAndImprintEffect(LAND) + STATIC BoostAttachedCreaturePerMatchingLandNameEffect(1,1) + equip — imprint land from library on ETB, equipped creature gets +1/+1 per matching land on all battlefields |
| Boost + equipped creature death trigger equip | `s/SylvokLifestaff.java` | STATIC BoostAttachedCreatureEffect(1, 0) + ON_EQUIPPED_CREATURE_DIES GainLifeEffect(3) + equip — triggers when the creature it's attached to dies |
| Grant ability + doesn't untap equip | `h/HeavyArbalest.java` | STATIC EquippedCreatureDoesntUntapEffect + GrantActivatedAbilityEffect(EQUIPPED_CREATURE) + equip |
| Per-blocker trigger equip | `i/InfiltrationLens.java` | ON_BECOMES_BLOCKED MayEffect(DrawCardEffect(2)) + `TriggerMode.PER_BLOCKER` + equip. Use `addEffect(slot, effect, TriggerMode.PER_BLOCKER)` for "becomes blocked by a creature" triggers that fire once per blocker |
| Becomes-target trigger equip | `l/LivewireLash.java` | ON_BECOMES_TARGET_OF_SPELL DealDamageToAnyTargetEffect(2) + STATIC BoostAttachedCreatureEffect(2, 0) + equip. Grants triggered ability to equipped creature: when it becomes the target of a spell, deal 2 damage to any target |
| Boost + grant card type equip | `s/SilverskinArmor.java` | STATIC BoostAttachedCreatureEffect(1, 1) + GrantCardTypeEffect(ARTIFACT, EQUIPPED_CREATURE) + equip. Makes equipped creature an artifact in addition to its other types (counts for metalcraft, artifact targeting, etc.) |

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
| Tap to lock artifact | `r/RustTick.java` | MayNotUntapDuringUntapStepEffect (STATIC) + TapTargetPermanentEffect + PreventTargetUntapWhileSourceTappedEffect + PermanentIsArtifactPredicate |
| Tap to draw | `a/ArcanisTheOmnipotent.java` | `(true, null, DrawCardEffect, false)` |
| Draw + discard | `m/MerfolkLooter.java` | DrawCardEffect + DiscardCardEffect |
| Grant keyword to target | `m/MightWeaver.java` | GrantKeywordEffect with color filter |
| Sacrifice self for effect | `b/BottleGnomes.java` | SacrificeSelfCost + effect |
| Sacrifice self + damage player/pw | `v/VulshokReplica.java` | SacrificeSelfCost + DealDamageToAnyTargetEffect + PermanentIsPlaneswalkerPredicate filter (restricts to players + planeswalkers) |
| Sacrifice self + choose source prevention | `a/AuriokReplica.java` | SacrificeSelfCost + PreventAllDamageFromChosenSourceEffect (prompts permanent choice on resolution) |
| Sacrifice subtype for effect | `s/SiegeGangCommander.java` | SacrificeSubtypeCreatureCost + DealDamageToAnyTargetEffect |
| Sacrifice artifact for effect | `b/BarrageOgre.java` | SacrificeArtifactCost + DealDamageToAnyTargetEffect (tap + sac artifact) |
| Sacrifice multiple permanents for tutor | `k/KuldothaForgemaster.java` | SacrificeMultiplePermanentsCost(3, PermanentIsArtifactPredicate) + SearchLibraryForCardTypesToBattlefieldEffect(ARTIFACT) (tap + sac 3 artifacts) |
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
| X-cost mass destroy (combat damage gated) | `s/SteelHellkite.java` | BoostSelfEffect pump + DestroyNonlandPermanentsWithManaValueXDealtCombatDamageEffect with maxActivationsPerTurn=1 — X-cost, once per turn, only affects damaged player's permanents |
| Loyalty (no target) | `a/AjaniOutlandChaperone.java` | `(+1, effects, false, description)` |
| Loyalty (with target filter) | `a/AjaniOutlandChaperone.java` | `(-2, effects, true, description, filter)` |
| Multi-target tap ability (equip mover) | `b/BrassSquire.java` | `ActivatedAbility(true, null, effects, desc, multiTargetFilters, 2, 2)` — tap to attach Equipment to creature, instant speed, uses `AttachTargetEquipmentToTargetCreatureEffect` |
