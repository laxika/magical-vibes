# CARD_PATTERN_INDEX

Purpose: quickly find a reference card for the pattern you're implementing. One or two examples per archetype. All paths relative to `cards/`.

## Lands

| Pattern | Reference | Notes |
|---------|-----------|-------|
| Basic land | `f/Forest.java` | `addEffect(ON_TAP, AwardManaEffect(color))` |
| Pain land | `s/SulfurousSprings.java` | 3 activated abilities: colorless + 2x colored with DealDamageToController |
| Creature land (manland) | `t/TreetopVillage.java` | STATIC `EntersTappedEffect` + ON_TAP mana + AnimateLandEffect ability |
| Creature land (artifact) | `i/InkmothNexus.java` | manland that becomes artifact creature (uses 6-arg AnimateLandEffect with grantedCardTypes) |
| Creature land + sub-ability | `s/SpawningPool.java` | manland + regenerate with `ONLY_WHILE_CREATURE` restriction |
| Check land | `d/DragonskullSummit.java` | STATIC EntersTappedUnlessControlLandSubtypeEffect(subtypes) + 2 mana abilities |
| Fast land | `b/BlackcleaveCliffs.java` | STATIC EntersTappedUnlessFewLandsEffect(2) + 2 mana abilities |
| Utility land | `q/Quicksand.java` | mana ability + sacrifice-to-debuff ability |
| Utility land (exile-return) | `m/MystifyingMaze.java` | colorless mana ability + {4},{T}: exile target attacking creature, return tapped at end step (ExileTargetPermanentAndReturnAtEndStepEffect(true) + PermanentIsAttackingPredicate) |

## Spells

| Pattern | Reference | Notes |
|---------|-----------|-------|
| Targeted burn | `s/Shock.java` | SPELL DealDamageToAnyTargetEffect (targeting auto-derived) |
| Burn creature + controller | `c/ChandrasOutrage.java` | DealDamageToTargetCreatureEffect + DealDamageToTargetCreatureControllerEffect |
| Uncounterable + unpreventable burn | `c/Combust.java` | STATIC CantBeCounteredEffect + DealDamageToTargetCreatureEffect(5, true) + PermanentColorInPredicate target filter |
| X burn | `b/Blaze.java` | DealXDamageToAnyTargetEffect |
| Burn + life drain | `e/EssenceDrain.java` | DealDamageToAnyTargetAndGainLifeEffect |
| Burn + drain by land count | `c/Corrupt.java` | DealDamageToAnyTargetEqualToControlledSubtypeCountAndGainLifeEffect — damage and life gain equal to controlled Swamps |
| X drain all opponents | `e/Exsanguinate.java` | EachOpponentLosesXLifeAndControllerGainsLifeLostEffect — no target, X life loss + gain |
| Multi-target damage | `c/ConeOfFlame.java` | DealOrderedDamageToAnyTargetsEffect |
| Any number of target players life gain | `h/HuntersFeast.java` | EachTargetPlayerGainsLifeEffect(6) + setMinTargets(0) + setMaxTargets(99) — any number of target players each gain N life |
| X burn divided evenly | `f/Fireball.java` | DealXDamageDividedEvenlyAmongTargetsEffect + setMinTargets(1) + setMaxTargets(99) + setAdditionalCostPerExtraTarget(1) — X divided evenly (rounded down) among any number of targets, costs {1} extra per target beyond first |
| Divided damage to filtered creatures | `i/IgniteDisorder.java` | DealDividedDamageAmongTargetCreaturesEffect(3) + setMinTargets(1) + setMaxTargets(3) + PermanentPredicateTargetFilter(PermanentAllOfPredicate(IsCreature + ColorIn)) — N damage divided as you choose among up to M target creatures matching a color/type filter |
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
| Targeted destroy (power filter) | `s/SmiteTheMonstrous.java` | DestroyTargetPermanentEffect + PermanentPowerAtLeastPredicate(4) creature filter |
| Multi-effect removal | `c/Condemn.java` | PutTargetOnBottomOfLibrary + GainLifeEqualToTargetToughness |
| Put on top of library | `b/BanishmentDecree.java` | PutTargetOnTopOfLibraryEffect + PermanentAnyOfPredicate filter (artifact/creature/enchantment) |
| Metalcraft sacrifice instant | `d/DispenseJustice.java` | SacrificeAttackingCreaturesEffect(1, 2) + PlayerPredicateTargetFilter(ANY) — metalcraft checked at resolution |
| Destroy + cantrip | `s/Smash.java` | DestroyTargetPermanentEffect + DrawCardEffect |
| Destroy + life gain by mana value | `d/DivineOffering.java` | DestroyTargetPermanentAndGainLifeEqualToManaValueEffect + artifact filter |
| Destroy + controller life loss | `g/GlissasScorn.java` | DestroyTargetPermanentAndControllerLosesLifeEffect(1) + artifact filter |
| Board wipe | `w/WrathOfGod.java` | DestroyAllPermanentsEffect |
| Board wipe + opponent library search to graveyard | `l/LifesFinale.java` | DestroyAllPermanentsEffect + SearchTargetLibraryForCardsToGraveyardEffect(3, CREATURE) + PlayerPredicateTargetFilter(OPPONENT) |
| Opponent land edict | `y/YawningFissure.java` | EachOpponentSacrificesPermanentsEffect(1, PermanentIsLandPredicate) — each opponent sacrifices a land, controller unaffected |
| Counter (any) | `c/Cancel.java` | CounterSpellEffect (spell targeting auto-derived) |
| Counter (filtered by type) | `r/RemoveSoul.java` | StackEntryPredicateTargetFilter + StackEntryTypeInPredicate |
| Counter (filtered by mana value) | `m/MentalMisstep.java` | StackEntryPredicateTargetFilter + StackEntryManaValuePredicate. Phyrexian mana cost |
| Counter + bonus | `d/Discombobulate.java` | Counter + ReorderTopCardsOfLibraryEffect |
| Counter + bounce | `l/LostInTheMist.java` | CounterSpellEffect + ReturnTargetPermanentToHandEffect — targets both a spell and a permanent. Uses `targetId` (spell, Zone.STACK) + `targetIds` (permanent). Multi-zone fizzle: only fizzles if ALL targets become illegal |
| Counter (filtered) + life loss | `p/PsychicBarrier.java` | TargetSpellControllerLosesLifeEffect(1) + CounterSpellEffect + creature-spell filter. Life loss placed before counter so target is still on stack |
| Counter-unless-pay + discard | `f/FrightfulDelusion.java` | TargetSpellControllerDiscardsEffect(1) + CounterUnlessPaysEffect(1). Discard placed before counter so target is still on stack |
| Counter + metalcraft cost reduction | `s/StoicRebuttal.java` | CounterSpellEffect + ReduceOwnCastCostIfMetalcraftEffect(1) — costs {1} less with 3+ artifacts |
| Counter (conditional, poisoned) | `c/CorruptedResolve.java` | CounterSpellIfControllerPoisonedEffect — counters only if target spell's controller is poisoned |
| Bounce target | `u/Unsummon.java` | ReturnTargetPermanentToHandEffect |
| Bounce target + life loss | `v/VaporSnag.java` | ReturnTargetPermanentToHandEffect(1) — bounce creature, its controller loses life |
| Bounce mass | `e/Evacuation.java` | ReturnCreaturesToOwnersHandEffect |
| Pure draw | `c/CounselOfTheSoratami.java` | DrawCardEffect |
| Draw + discard | `s/Sift.java` | DrawCardEffect + DiscardCardEffect |
| Each player draw + random discard | `b/BurningInquiry.java` | EachPlayerDrawsCardEffect + EachPlayerRandomDiscardEffect |
| Library selection (hand/top/bottom) | `t/TellingTime.java` | LookAtTopCardsHandTopBottomEffect |
| Library selection (N to hand, rest to graveyard) | `f/ForbiddenAlchemy.java` | LookAtTopCardsChooseNToHandRestToGraveyardEffect(count, toHandCount) |
| Library selection + self-damage | `d/DarkBargain.java` | LookAtTopCardsChooseNToHandRestToGraveyardEffect(3, 2) + DealDamageToControllerEffect(2) |
| Library reveal (type to hand, rest to graveyard) | `m/Mulch.java` | RevealTopCardsTypeToHandRestToGraveyardEffect(count, cardTypes) — deterministic, no player choice |
| Library match-permanent-to-battlefield | `m/MitoticManipulation.java` | LookAtTopCardsPutMatchingPermanentNameOnBattlefieldEffect |
| Targeted discard | `d/Distress.java` | ChooseCardFromTargetHandToDiscardEffect |
| Exile by name (multi-zone) | `m/Memoricide.java` | ChooseCardNameAndExileFromZonesEffect(excludedTypes) — choose nonland name, exile from hand+graveyard+library, shuffle |
| Tutor to hand | `d/DiabolicTutor.java` | SearchLibraryForCardToHandEffect |
| Tutor + exile + opponent choice | `d/DistantMemories.java` | DistantMemoriesEffect — search, exile, opponent may let you have it or draw 3 |
| Tutor to battlefield | `r/RampantGrowth.java` | SearchLibraryForCardTypesToBattlefieldEffect |
| Cultivate (2 basic lands split) | `c/Cultivate.java` | SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect — one to BF tapped, one to hand |
| Graveyard return (to hand) | `r/Recollect.java` | ReturnCardFromGraveyardEffect.builder().destination(HAND).targetGraveyard(true).build() — any card, targets graveyard |
| Graveyard return (multi-target to hand) | `m/MorbidPlunder.java` | ReturnTargetCardsFromGraveyardToHandEffect(CardTypePredicate(CREATURE), 2) — up to N target cards to hand |
| Graveyard return (to battlefield) | `b/BeaconOfUnrest.java` | ReturnCardFromGraveyardEffect.builder().destination(BATTLEFIELD).filter(CardAnyOfPredicate).source(ALL_GRAVEYARDS).build() |
| Graveyard reanimate + type/color grant | `r/RiseFromTheGrave.java` | ReturnCardFromGraveyardEffect.builder().destination(BATTLEFIELD).source(ALL_GRAVEYARDS).grantColor(BLACK).grantSubtype(ZOMBIE).build() — permanently adds color and subtype "in addition to" |
| Graveyard to top of owner's library | `n/NoxiousRevival.java` | ReturnCardFromGraveyardEffect.builder().destination(TOP_OF_OWNERS_LIBRARY).source(ALL_GRAVEYARDS).targetGraveyard(true).build() — any card from any graveyard on top of owner's library. Phyrexian mana |
| Graveyard to top of library + draw | `f/FranticSalvage.java` | PutTargetCardsFromGraveyardOnTopOfLibraryEffect(CardTypePredicate(ARTIFACT)) + DrawCardEffect — any number of target artifact cards, multi-graveyard targeting at cast time |
| Graveyard shuffle into library (target player) | `m/MemorysJourney.java` | ShuffleTargetCardsFromGraveyardIntoLibraryEffect(null, 3) + FlashbackCast("{G}") — target player shuffles up to 3 cards from their graveyard into their library, targets any player's graveyard |
| Graveyard shuffle + draw + self-mill trigger | `g/GaeasBlessing.java` | ShuffleTargetCardsFromGraveyardIntoLibraryEffect(null, 3) + DrawCardEffect(1) + ON_SELF_MILLED ShuffleGraveyardIntoLibraryEffect — shuffle up to 3 cards from target player's graveyard into library, draw, and when milled shuffle owner's graveyard into library |
| Exile return to hand (filtered, owned) | `r/RunicRepetition.java` | ReturnTargetCardFromExileToHandEffect(CardHasFlashbackPredicate(), true) — return target exiled card with flashback you own to hand. `ownedOnly=true` restricts to controller's exile zone. Targets exile (`canTargetExile()=true`) |
| Prevent combat damage | `h/HolyDay.java` | PreventAllCombatDamageEffect |
| Prevent combat damage (selective) | `m/Moonmist.java` | TransformAllEffect(PermanentHasSubtypePredicate(HUMAN)) + PreventCombatDamageExceptBySubtypesEffect(PermanentHasAnySubtypePredicate(WEREWOLF, WOLF)) — transform all Humans, prevent combat damage by non-Werewolves/Wolves |
| Steal creature (temp) | `t/Threaten.java` | GainControlOfTargetCreatureUntilEndOfTurn + haste + untap |
| Steal artifact (temp) | `m/MetallicMastery.java` | GainControlOfTargetPermanentUntilEndOfTurn + untap + haste + PermanentIsArtifactPredicate filter |
| Extra turn | `t/TimeStretch.java` | ExtraTurnEffect |
| Extra combat | `r/RelentlessAssault.java` | AdditionalCombatMainPhaseEffect |
| Mill | `t/Traumatize.java` | MillHalfLibraryEffect |
| Shuffle-back spell | `b/BeaconOfDestruction.java` | Effect + ShuffleIntoLibraryEffect |
| X draw (controller) | `m/MindSpring.java` | DrawXCardsEffect — non-targeting X draw for controller |
| X burn + exile-instead-of-die + shuffle | `r/RedSunsZenith.java` | DealXDamageToAnyTargetEffect(true) + ShuffleIntoLibraryEffect |
| X tokens + shuffle | `w/WhiteSunsZenith.java` | CreateXCreatureTokenEffect + ShuffleIntoLibraryEffect |
| Fight (two-target) | `p/PreyUpon.java` | FirstTargetFightsSecondTargetEffect, multi-target: creature you control + creature you don't control |
| Bite (pump + bite) | `a/AssertPerfection.java` | BoostFirstTargetCreatureEffect + FirstTargetDealsPowerDamageToSecondTargetEffect, multi-target with per-position filters |
| Pump + debuff (two targets) | `l/LeechingBite.java` | BoostFirstTargetCreatureEffect + BoostSecondTargetCreatureEffect, multi-target with creature filters |
| Damage creature + destroy equipment | `t/TurnToSlag.java` | DestroyEquipmentAttachedToTargetCreatureEffect + DealDamageToTargetCreatureEffect — effect order doesn't matter; lethal damage destruction is deferred until all effects on the stack entry resolve |
| Sacrifice artifact spell cost + tokens | `k/KuldothaRebirth.java` | SacrificeArtifactCost + CreateCreatureTokenEffect — sacrifice artifact as additional spell cost |
| Sacrifice permanent spell cost + burn | `a/Artillerize.java` | SacrificePermanentCost(PermanentAnyOfPredicate) + DealDamageToAnyTargetEffect — sacrifice artifact or creature as additional spell cost |
| Sacrifice creature spell cost + power-based mass debuff | `i/IchorExplosion.java` | SacrificeCreatureCost(false, true) + BoostAllCreaturesXEffect(-1, -1) — sacrifice creature, all creatures get -X/-X where X = sacrificed creature's power |
| Exile graveyard creature spell cost + power-based damage | `c/CorpseLunge.java` | ExileCardFromGraveyardCost(CREATURE, false, false, true) + DealXDamageToTargetCreatureEffect — exile creature from graveyard as additional cost, deal damage equal to exiled card's power to target creature |
| Graveyard-count damage | `s/ScrapyardSalvo.java` | DealDamageToTargetPlayerEqualToCardTypeCountInGraveyardEffect(ARTIFACT) — damage to target player equal to artifact cards in graveyard |
| Mass exile + reveal creatures to battlefield | `m/MassPolymorph.java` | ExileAllCreaturesYouControlThenRevealCreaturesToBattlefieldEffect — exile all your creatures, reveal from library until that many creature cards found, put onto battlefield, shuffle rest back |

| Static self-boost per controlled subtype | `e/EarthServant.java` | STATIC BoostSelfPerControlledSubtypeEffect(MOUNTAIN, 0, 1) — +0/+1 for each Mountain you control |
| Flashback spell | `a/AncientGrudge.java` | `addCastingOption(new FlashbackCast("{G}"))` + normal effects/targeting. Cast from graveyard for flashback cost, exiled after resolving or fizzling. Flashback is a spell cast (counterable, triggers "whenever you cast"), not an activated ability. |
| Exile target + all same name | `s/SeverTheBloodline.java` | ExileTargetCreatureAndAllWithSameNameEffect + FlashbackCast("{5}{B}{B}") — exile target creature and all other creatures with the same name |
| Grant flashback to graveyard | `p/PastInFlames.java` | `GrantFlashbackToGraveyardCardsEffect(Set.of(CardType.INSTANT, CardType.SORCERY))` + own `FlashbackCast("{4}{R}")`. Grants flashback (cost = mana cost) to matching cards in controller's graveyard until end of turn. Tracked in `GameData.cardsGrantedFlashbackUntilEndOfTurn`. |
| ETB grant flashback to target graveyard card | `s/SnapcasterMage.java` | `GrantFlashbackToTargetGraveyardCardEffect(Set.of(CardType.INSTANT, CardType.SORCERY))` on ON_ENTER_BATTLEFIELD. Flash creature. Targets a single instant/sorcery in controller's graveyard, grants flashback (cost = mana cost) until end of turn. Uses multi-graveyard targeting at ETB trigger time. |

## Vanilla creatures (empty body, all from Scryfall)

Reference: `a/AirElemental.java` — no constructor code needed.

## Keyword creatures (keywords from Scryfall, empty body)

| Pattern | Reference | Notes |
|---------|-----------|-------|
| Infect creature | `b/BlackcleaveGoblin.java` | Haste + Infect auto-loaded from Scryfall. Infect deals damage as -1/-1 counters to creatures and poison counters to players. |
| Infect + damage replacement | `p/PhyrexianHydra.java` | Infect from Scryfall + STATIC PreventDamageAndAddMinusCountersEffect. Prevents all damage to self and puts -1/-1 counters instead. |
| Intimidate creature + activated ability | `g/GethLordOfTheVault.java` | Intimidate from Scryfall + X-cost graveyard-targeting activated ability. PutCardFromOpponentGraveyardOntoBattlefieldEffect(tapped=true) |
| Keyword creature + shuffle-into-library replacement | `b/BlightsteelColossus.java` | Keywords (infect, trample, indestructible) auto-loaded from Scryfall + STATIC `ShuffleIntoLibraryReplacementEffect`. Replacement effect: when put into graveyard from anywhere, shuffled into owner's library instead. Also used by `l/LegacyWeapon.java`. |
| Keyword creature + exile-with-egg-counters death replacement | `d/DarigaazReincarnated.java` | Keywords (flying, trample, haste) auto-loaded from Scryfall + STATIC `ExileWithEggCountersInsteadOfDyingEffect(3)`. "If would die, instead exile with 3 egg counters." Upkeep trigger removes 1 counter per owner's upkeep; returns to battlefield when last counter removed. Counter tracking via `GameData.exiledCardEggCounters`. |
| Can't be countered + keyword + ability | `t/ThrunTheLastTroll.java` | STATIC `CantBeCounteredEffect` + hexproof from Scryfall + `{1}{G}` RegenerateEffect activated ability. Checked by `GameQueryService.isUncounterable()`. |
| Alternate casting cost (sacrifice + life) | `d/DemonOfDeathsGate.java` | `addCastingOption(new AlternateHandCast(List.of(new LifeCastingCost(N), new SacrificePermanentsCost(M, predicate))))` — "You may pay N life and sacrifice M [matching] creatures rather than pay this spell's mana cost." Frontend sends `alternateCostSacrificePermanentIds` in PlayCardRequest. Keywords (flying, trample) auto-loaded from Scryfall. |
| ETB + discard-to-battlefield replacement | `o/ObstinateBaloth.java` | ON_SELF_DISCARDED_BY_OPPONENT `EnterBattlefieldOnDiscardEffect` + ON_ENTER_BATTLEFIELD GainLifeEffect. Replacement effect: if opponent causes you to discard this card, put it onto the battlefield instead of graveyard. ETB triggers still fire. |

## ETB creatures

| Pattern | Reference | Notes |
|---------|-----------|-------|
| ETB gain life | `a/AngelOfMercy.java` | ON_ENTER_BATTLEFIELD GainLifeEffect |
| ETB draw | `k/KavuClimber.java` | ON_ENTER_BATTLEFIELD DrawCardEffect |
| ETB self-mill | `a/ArmoredSkaab.java` | ON_ENTER_BATTLEFIELD MillControllerEffect(4) — controller mills N cards, no target |
| ETB draw + downside | `p/PhyrexianRager.java` | Draw + LoseLifeEffect |
| ETB destroy (targeted) | `n/Nekrataal.java` | ON_ENTER_BATTLEFIELD DestroyTargetPermanentEffect (targeting auto-derived) |
| ETB may destroy (filtered) | `a/AcidWebSpider.java` | MayEffect(DestroyTargetPermanentEffect) + PermanentPredicateTargetFilter |
| ETB destroy all (predicate) + static hexproof | `w/WitchbaneOrb.java` | DestroyAllPermanentsEffect(AllOf(HasSubtype(CURSE), AttachedToSourceController)) + GrantControllerHexproofEffect STATIC |
| ETB may exile until leaves (O-ring) | `l/LeoninRelicWarder.java` | MayEffect(ExileTargetPermanentUntilSourceLeavesEffect) + PermanentPredicateTargetFilter(AnyOf(artifact, enchantment)). Exiled card returns when source leaves battlefield |
| ETB may rummage (discard then draw) | `k/KeldonRaider.java` | MayEffect(DiscardAndDrawCardEffect()) — may discard a card, if you do draw a card |
| ETB discard (targeted) | `r/RavenousRats.java` | TargetPlayerDiscardsEffect |
| ETB discard (each opponent) | `l/LilianasSpecter.java` | EachOpponentDiscardsEffect — no targeting, all opponents discard |
| ETB search | `c/CivicWayfinder.java` | MayEffect(SearchLibraryForBasicLandToHandEffect) |
| ETB search (type + min MV) | `t/TreasureMage.java` | MayEffect(SearchLibraryForCardTypesToHandEffect(ARTIFACT, 6, MAX_VALUE)) — artifact with MV 6+ |
| ETB search (by name, multi-pick) | `s/SquadronHawk.java` | MayEffect(SearchLibraryForCardsByNameToHandEffect("Squadron Hawk", 3)) — search for up to 3 copies by name to hand |
| ETB may return from GY | `g/Gravedigger.java` | MayEffect(ReturnCardFromGraveyardEffect.builder().destination(HAND).filter(CardTypePredicate(CREATURE)).build()) |
| ETB may bounce own historic | `g/GuardiansOfKoilos.java` | MayEffect(ReturnTargetPermanentToHandEffect()) + PermanentPredicateTargetFilter(AllOf(AnyOf(artifact, legendary, Saga), controlled-by-source, not-source)) — "you may return another target historic permanent you control to its owner's hand" |
| ETB cast from opponent's GY | `c/ChancellorOfTheSpires.java` | CastTargetInstantOrSorceryFromGraveyardEffect(OPPONENT_GRAVEYARD, true) — targets instant/sorcery in opponent's graveyard, may cast without paying. Also has ON_OPENING_HAND_REVEAL MayEffect(EachOpponentMillsEffect(7)) |
| ETB tokens + ability | `s/SiegeGangCommander.java` | CreateCreatureTokenEffect + activated sac ability |
| ETB copy | `c/Clone.java` | CopyPermanentOnEnterEffect |
| ETB copy with P/T override | `q/QuicksilverGargantuan.java` | CopyPermanentOnEnterEffect(filter, typeLabel, 7, 7) — "copy except it's 7/7" |
| ETB copy with type override | `p/PhyrexianMetamorph.java` | CopyPermanentOnEnterEffect(AnyOfPredicate, typeLabel, null, null, Set.of(ARTIFACT)) — "copy except it's also an artifact" |
| ETB copy with extra ability | `e/EvilTwin.java` | CopyPermanentOnEnterEffect with additionalActivatedAbilities — "copy except it has {U}{B},{T}: Destroy target creature with the same name" |
| ETB choose color | `v/VoiceOfAll.java` | ProtectionFromChosenColorEffect |
| ETB choose name | `p/PithingNeedle.java` | ChooseCardNameOnEnterEffect + static lock |
| ETB choose nonland name | `p/PhyrexianRevoker.java` | ChooseCardNameOnEnterEffect(List.of(LAND)) + static lock — artifact creature variant |
| As-enters choose creature + sacrifice grant | `d/DauntlessBodyguard.java` | ChooseAnotherCreatureOnEnterEffect + SacrificeSelfCost + GrantKeywordToChosenCreatureUntilEndOfTurnEffect — choose another creature on entry (replacement effect, not Torpor Orb-able), sacrifice to grant chosen creature indestructible |
| ETB control handoff | `s/SleeperAgent.java` | TargetPlayerGainsControlOfSourceCreatureEffect |
| ETB drawback (discard) | `h/HiddenHorror.java` | SacrificeUnlessDiscardCardTypeEffect |
| ETB drawback (bounce artifact) | `g/GlintHawk.java` | SacrificeUnlessReturnOwnPermanentTypeToHandEffect(ARTIFACT) — sacrifice unless return own artifact to hand |
| ETB -1/-1 counters + counter removal ability | `b/BurdenedStoneback.java` | PutCountersOnSourceEffect(-1,-1,2) + RemoveCounterFromSourceCost + GrantKeywordEffect |
| ETB -1/-1 counters + mass -1/-1 ability | `c/CarnifexDemon.java` | PutCountersOnSourceEffect(-1,-1,2) + RemoveCounterFromSourceCost + PutMinusOneMinusOneCounterOnEachOtherCreatureEffect |
| ETB -1/-1 counters + multi-counter removal + player draw | `e/EtchedMonstrosity.java` | PutCountersOnSourceEffect(-1,-1,5) + RemoveCounterFromSourceCost(5) + DrawCardForTargetPlayerEffect(3, false, true) with PlayerPredicateTargetFilter |
| ETB -1/-1 counters on target | `s/Skinrender.java` | PutMinusOneMinusOneCounterOnTargetCreatureEffect(3) + PermanentIsCreaturePredicate filter |
| ETB controls-another-subtype conditional damage | `g/GhituJourneymage.java` | ControlsAnotherSubtypeConditionalEffect(WIZARD, DealDamageToEachOpponentEffect(2)) — intervening-if another Wizard |
| ETB metalcraft conditional drain | `b/BleakCovenVampires.java` | MetalcraftConditionalEffect(TargetPlayerLosesLifeAndControllerGainsLifeEffect) — intervening-if 3+ artifacts |
| ETB metalcraft conditional boost + haste | `b/BladeTribeBerserkers.java` | Two MetalcraftConditionalEffect wrappers: BoostSelfEffect(3,3) + GrantKeywordEffect(HASTE, SELF) — multiple wrapped effects on same slot |
| ETB +1/+1 counters per subtype | `u/UnbreathingHorde.java` | ON_ENTER_BATTLEFIELD EnterWithPlusOnePlusOneCountersPerSubtypeEffect(ZOMBIE, true) + STATIC PreventDamageAndRemovePlusOnePlusOneCountersEffect(true) — enters with counters per Zombie on battlefield + graveyard, damage prevents and removes one counter |
| ETB each player poison | `i/IchorRats.java` | ON_ENTER_BATTLEFIELD GiveEachPlayerPoisonCountersEffect(1) — infect creature, each player gets a poison counter |
| ETB may-sacrifice-artifact-divided-damage | `k/KuldothaFlamefiend.java` | MayEffect(SacrificeArtifactThenDealDividedDamageEffect(4)) — may sacrifice artifact, deal N divided damage. Damage assignments via `pendingETBDamageAssignments`, artifact choice via PermanentChoiceContext.SacrificeArtifactForDividedDamage |
| ETB+attack divided damage + firebreathing | `i/InfernoTitan.java` | DealDividedDamageToAnyTargetsEffect(3, 3) on ON_ENTER_BATTLEFIELD + ON_ATTACK, plus {R} BoostSelfEffect(1,0) activated ability. Damage assignments via `pendingETBDamageAssignments` |
| ETB+attack may-search multi-land | `p/PrimevalTitan.java` | MayEffect(SearchLibraryForCardTypesToBattlefieldEffect(LAND, false, true, 2)) on ON_ENTER_BATTLEFIELD + ON_ATTACK. Multi-pick search via maxCount=2. MayEffect separated from mandatory effects in CombatAttackService for attack triggers |
| ETB modal (choose one, no target) | `b/BrutalizerExarch.java` | ChooseOneEffect on ON_ENTER_BATTLEFIELD with one non-targeting mode (library search) and one targeting mode. Card-level setTargetFilter for the targeting mode. Mode chosen at cast time via xValue |
| ETB modal (choose one, per-mode targeting) | `d/DeceiverExarch.java` | ChooseOneEffect on ON_ENTER_BATTLEFIELD with per-mode TargetFilter on each ChooseOneOption. Mode 0: UntapTargetPermanentEffect + PermanentControlledBySourceControllerPredicate. Mode 1: TapTargetPermanentEffect + PermanentNotPredicate(PermanentControlledBySourceControllerPredicate). Flash creature |
| ETB search-exile-imprint + death return to hand | `h/HoardingDragon.java` | MayEffect(SearchLibraryForCardTypeToExileAndImprintEffect(ARTIFACT)) + MayEffect(PutImprintedCardIntoOwnersHandEffect) — ETB searches library for artifact to exile/imprint, death returns exiled card to owner's hand |
| Kicker creature (ETB counters if kicked) | `a/AcademyDrake.java` | STATIC KickerEffect("{4}") + ON_ENTER_BATTLEFIELD EnterWithPlusOnePlusOneCountersIfKickedEffect(2) — kicker is optional additional mana cost, if paid the creature enters with +1/+1 counters |
| Kicker instant (conditional spell effect) | `b/BlinkOfAnEye.java` | STATIC KickerEffect("{1}{U}") + SPELL ReturnTargetPermanentToHandEffect() + SPELL KickedConditionalEffect(DrawCardEffect()) — bounce nonland permanent, if kicked also draw a card. Use KickedConditionalEffect to wrap any spell effect that should only resolve when kicked |
| Kicker sorcery (replacement effect) | `f/FightWithFire.java` | STATIC KickerEffect("{5}{R}") + SPELL KickerReplacementEffect(DealDamageToTargetCreatureEffect(5), DealDividedDamageAmongAnyTargetsEffect(10)) — deals 5 damage to target creature; if kicked, deals 10 damage divided among any targets instead. Use KickerReplacementEffect when kicked mode *replaces* the base mode ("instead" in oracle text) |
| Kicker sorcery (sacrifice kicker + additional target) | `g/GoblinBarrage.java` | STATIC KickerEffect(PermanentAnyOfPredicate(...), "an artifact or Goblin") + SPELL DealDamageToTargetCreatureEffect(4) + SPELL KickedConditionalEffect(DealDamageToSecondaryTargetEffect(4)) — sacrifice-based kicker cost with additional player target when kicked. Primary target in targetId, kicked target in targetIds |

## Triggered creatures

| Pattern | Reference | Notes |
|---------|-----------|-------|
| On death | `b/BogardanFirefiend.java` | ON_DEATH DealDamageToTargetCreatureEffect |
| On death with cascading tokens | `m/MitoticSlime.java` | ON_DEATH CreateCreatureTokenEffect with TokenEffectEntry — tokens themselves have death triggers creating smaller tokens |
| On death exile self + random return | `m/MoldgrafMonstrosity.java` | ON_DEATH ReturnCardFromGraveyardEffect with `exileSourceFromGraveyard(true)` + `returnAtRandom(true)` + `randomCount(2)` + creature filter — exiles self then returns random creatures to battlefield |
| Upkeep sacrifice/discard | `r/RazormaneMasticore.java` | UPKEEP_TRIGGERED + DRAW_TRIGGERED |
| Upkeep sacrifice other + opponent life loss | `x/XathridDemon.java` | UPKEEP_TRIGGERED SacrificeOtherCreatureOpponentsLoseLifeOrTapAndLoseLifeEffect(7) — sacrifice other creature, opponents lose life equal to its power; or tap self + lose 7 life |
| Exile-from-graveyard cost + damage | `m/MoltenTailMasticore.java` | ExileCardFromGraveyardCost(CREATURE) + DealDamageToAnyTargetEffect + RegenerateEffect |
| Exile-N-creatures-from-graveyard + cast from graveyard | `s/SkaabRuinator.java` | ExileNCardsFromGraveyardCost(3, CREATURE) + GraveyardCast — creature with "exile 3 creature cards from graveyard" additional cost and "you may cast from your graveyard" ability |
| Attack token per creature in graveyard | `k/KessigCagebreakers.java` | ON_ATTACK CreateTokensPerCreatureCardInGraveyardEffect("Wolf", 2, 2, GREEN, WOLF, true) — creates tapped-and-attacking tokens equal to creature cards in graveyard |
| Ally-creatures-attack mana per attacker | `g/GrandWarlordRadha.java` | ON_ALLY_CREATURES_ATTACK AddManaPerAttackingCreatureEffect(RED, GREEN) — fires once when 1+ creatures attack, adds mana = attacker count (player chooses R or G), prevents mana drain until end of turn |
| Enters tapped + doesn't untap + sac-another-creature untap + attack trigger destroy+counter | `g/GrimgrinCorpseBorn.java` | EntersTappedEffect + DoesntUntapDuringUntapStepEffect + SacrificeCreatureCost(excludeSelf=true)+UntapSelfEffect+PutCountersOnSourceEffect(1,1,1) activated ability + ON_ATTACK DestroyTargetPermanentEffect+PutCountersOnSourceEffect(1,1,1) with PermanentPredicateTargetFilter for opponent creatures |
| Upkeep bounce | `s/StampedingWildebeests.java` | BounceCreatureOnUpkeepEffect |
| Upkeep token | `v/VerdantForce.java` | EACH_UPKEEP_TRIGGERED CreateCreatureTokenEffect |
| Upkeep conditional self-bounce + tokens | `t/ThopterAssembly.java` | UPKEEP_TRIGGERED NoOtherSubtypeConditionalEffect(THOPTER, ReturnSelfToHandAndCreateTokensEffect) — intervening-if "no other Thopters", returns self + creates 5 tokens |
| Upkeep token per equipment | `k/KembaKhaRegent.java` | UPKEEP_TRIGGERED CreateTokenPerEquipmentOnSourceEffect — tokens equal to attached Equipment |
| Upkeep may target artifact charge | `v/VedalkenInfuser.java` | UPKEEP_TRIGGERED MayEffect(PutChargeCounterOnTargetPermanentEffect) + PermanentPredicateTargetFilter(PermanentIsArtifactPredicate) — may put charge counter on target artifact |
| Graveyard upkeep | `s/SqueeGoblinNabob.java` | GRAVEYARD_UPKEEP_TRIGGERED ReturnCardFromGraveyardEffect.builder().destination(HAND).filter(CardIsSelfPredicate).returnAll(true).build() |
| Graveyard metalcraft pay-to-return | `k/KuldothaPhoenix.java` | GRAVEYARD_UPKEEP_TRIGGERED MetalcraftConditionalEffect(MayPayManaEffect("{4}", ReturnCardFromGraveyardEffect.builder().destination(BATTLEFIELD).filter(CardIsSelfPredicate).build())) — metalcraft checked at trigger time, mana paid at resolution |
| Graveyard spell-cast pay-to-return | `l/LingeringPhantom.java` | GRAVEYARD_ON_CONTROLLER_CASTS_SPELL SpellCastTriggerEffect(CardIsHistoricPredicate, ReturnCardFromGraveyardEffect.builder().destination(HAND).filter(CardIsSelfPredicate).returnAll(true).build(), "{B}") — triggers from graveyard when controller casts historic spell, may pay {B} to return self to hand |
| Graveyard activated ability (return to hand) | `m/MagmaPhoenix.java` | `addGraveyardActivatedAbility(ActivatedAbility(false, "{3}{R}{R}", ReturnCardFromGraveyardEffect.builder().destination(HAND).filter(CardIsSelfPredicate).build()))` — activated ability usable from graveyard zone, pays mana, puts on stack as ACTIVATED_ABILITY. Blocked by Pithing Needle |
| Graveyard activated ability (return to battlefield tapped) | `r/ReassemblingSkeleton.java` | `addGraveyardActivatedAbility(ActivatedAbility(false, "{1}{B}", ReturnCardFromGraveyardEffect.builder().destination(BATTLEFIELD).filter(CardIsSelfPredicate).enterTapped(true).build()))` — self-return to battlefield tapped from graveyard |
| Combat damage to player | `t/ThievingMagpie.java` | ON_COMBAT_DAMAGE_TO_PLAYER DrawCardEffect |
| Combat damage awakening | `l/LiegeOfTheTangle.java` | ON_COMBAT_DAMAGE_TO_PLAYER PutAwakeningCountersOnTargetLandsEffect — multi-permanent choice on controller's lands, permanent 8/8 animation via awakening counters |
| Combat damage may-sacrifice-draw | `i/ImpalerShrike.java` | ON_COMBAT_DAMAGE_TO_PLAYER `MayEffect(SacrificeSelfAndDrawCardsEffect(3))` — may sacrifice self, if you do draw N cards |
| Combat damage may-sacrifice-destroy | `b/BlindZealot.java` | ON_COMBAT_DAMAGE_TO_PLAYER `MayEffect(SacrificeSelfToDestroyCreatureDamagedPlayerControlsEffect)` — MayEffect wraps inner sacrifice+destroy. CombatService queues as PendingMayAbility with context; after acceptance, inner effect presents multi-permanent choice for target creature |
| Combat damage may-sacrifice-discard-per-poison | `w/WhisperingSpecter.java` | ON_COMBAT_DAMAGE_TO_PLAYER `MayEffect(SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect)` — may sacrifice self, if you do damaged player discards cards equal to their poison counter count |
| On becomes blocked | `s/SylvanBasilisk.java` | ON_BECOMES_BLOCKED DestroyCreatureBlockingThisEffect |
| On block (mutual destroy) | `l/LoyalSentry.java` | ON_BLOCK DestroyBlockedCreatureAndSelfEffect |
| On block conditional boost | `e/EzurisArchers.java` | ON_BLOCK BoostSelfWhenBlockingKeywordEffect(FLYING, 3, 0) — conditional trigger checked at block time |
| On damaged creature dies | `s/SengirVampire.java` | ON_DAMAGED_CREATURE_DIES PutPlusOnePlusOneCounterOnSourceEffect |
| On controller gains life | `a/AjanisPridemate.java` | ON_CONTROLLER_GAINS_LIFE PutCountersOnSourceEffect(1,1,1) — +1/+1 counter whenever controller gains life |
| On ally creature with subtype enters | `c/ChampionOfTheParish.java` | ON_ALLY_CREATURE_ENTERS_BATTLEFIELD SubtypeConditionalEffect(HUMAN, PutCountersOnSourceEffect(1,1,1)) — +1/+1 counter whenever another Human you control enters |
| On dealt damage to self | `n/NestedGhoul.java` | ON_DEALT_DAMAGE CreateCreatureTokenEffect — fires per source, both combat and non-combat |
| On dealt damage: source controller poison | `r/ReaperOfSheoldred.java` | ON_DEALT_DAMAGE DamageSourceControllerGetsPoisonCounterEffect(null) marker — UUID filled at trigger time, gives source's controller 1 poison counter |
| On dealt damage: source controller sacrifices | `p/PhyrexianObliterator.java` | ON_DEALT_DAMAGE DamageSourceControllerSacrificesPermanentsEffect(0,null) marker — count+playerId filled at trigger time, multi-permanent choice for sacrifice |
| On opponent creature dealt damage | `k/KazarovSengirPureblood.java` | ON_OPPONENT_CREATURE_DEALT_DAMAGE PutCounterOnSelfEffect(PLUS_ONE_PLUS_ONE) — fires per damaged creature (combat + non-combat), scans all battlefields |
| Other creature enters | `s/SoulWarden.java` | ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD GainLifeEffect |
| Ally creature enters (may gain life) + opponent creature enters (may lose life) | `s/SuturePriest.java` | ON_ALLY_CREATURE_ENTERS_BATTLEFIELD MayEffect(GainLifeEffect(1)) + ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD MayEffect(TargetPlayerLosesLifeEffect(1)) |
| Ally creature dies | `g/GravePact.java` | ON_ALLY_CREATURE_DIES EachOpponentSacrificesCreatureEffect |
| Any creature dies (may loot) | `m/MurderOfCrows.java` | ON_ANY_CREATURE_DIES MayEffect(DrawAndDiscardCardEffect()) — may draw+discard on any creature death |
| Opponent creature dies | `g/GlissaTheTraitor.java` | ON_OPPONENT_CREATURE_DIES MayEffect(ReturnCardFromGraveyardEffect.builder().destination(HAND).filter(CardTypePredicate(ARTIFACT)).build()) — includes tokens |
| Any artifact goes to graveyard from battlefield | `m/MolderBeast.java` | ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD BoostSelfEffect |
| Any artifact goes to graveyard — damage controller | `m/MagneticMine.java` | ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD DealDamageToTriggeringPermanentControllerEffect(2) — target pre-set to artifact's controller at trigger time |
| Artifact put into opponent's graveyard from battlefield | `v/ViridianRevel.java` | ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD MayEffect(DrawCardEffect) |
| Opponent dealt noncombat damage — self boost | `c/ChandrasSpitfire.java` | ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE BoostSelfEffect(3, 0) — +3/+0 whenever an opponent is dealt noncombat damage |
| Opponent draws | `u/UnderworldDreams.java` | ON_OPPONENT_DRAWS DealDamageToTargetPlayerEffect |
| Opponent loses life — mill | `m/Mindcrank.java` | ON_OPPONENT_LOSES_LIFE MillOpponentOnLifeLossEffect — whenever opponent loses life, mills that many cards. Trigger fires from damage (spell + combat) and life loss effects |
| Opponent discards — damage | `m/Megrim.java` | ON_OPPONENT_DISCARDS DealDamageToDiscardingPlayerEffect |
| Opponent discards — life loss | `l/LilianasCaress.java` | ON_OPPONENT_DISCARDS LoseLifeEffect |
| Spell cast trigger | `q/QuirionDryad.java` | ON_ANY_PLAYER_CASTS_SPELL +1/+1 counter |
| Historic spell cast trigger (damage) | `c/CabalPaladin.java` | ON_CONTROLLER_CASTS_SPELL SpellCastTriggerEffect(CardIsHistoricPredicate, DealDamageToEachOpponentEffect(2)) — deals 2 damage to each opponent when casting historic spells |
| Kicked spell cast trigger (counter + damage) | `h/HallarTheFirefletcher.java` | ON_CONTROLLER_CASTS_SPELL KickedSpellCastTriggerEffect(PutCountersOnSourceEffect(1,1,1), DealDamageToEachOpponentEqualToPlusOnePlusOneCountersOnSourceEffect) — on kicked spell cast, add +1/+1 counter then deal damage equal to total counters to each opponent |
| Knowledge Pool-style cast intercept | `k/KnowledgePool.java` | ON_ENTER_BATTLEFIELD EachPlayerExilesTopCardsToSourceEffect(3) + ON_ANY_PLAYER_CASTS_SPELL KnowledgePoolCastTriggerEffect — exiles cast-from-hand spells, lets caster pick a nonland card from pool to cast free. Uses per-permanent exile tracking (permanentExiledCards) and KNOWLEDGE_POOL_CAST_CHOICE awaiting input |
| May gain life on spell cast | `a/AngelsFeather.java` | MayEffect(GainLifeOnSpellCastEffect(CardColorPredicate)) — also `g/GolemsHeart.java` with CardTypePredicate |
| May pay to deal damage on artifact cast | `e/Embersmith.java` | MayEffect(DealDamageToAnyTargetOnArtifactCastEffect) — pay mana + any-target trigger |
| May pay to create token on artifact cast | `m/Myrsmith.java` | MayEffect(CreateTokenOnOwnSpellCastWithCostEffect) — pay mana + create token trigger |
| May loot on spell cast | `r/Riddlesmith.java` | MayEffect(DrawAndDiscardOnOwnSpellCastEffect(CardTypePredicate)) — may draw+discard on matching spell cast |
| Opponent spell punisher (discard or life) | `p/PainfulQuandary.java` | ON_OPPONENT_CASTS_SPELL LoseLifeUnlessDiscardEffect(5) — opponent chooses discard or lose life |
| Opponent spell tax (counter unless pays) | `c/ChancellorOfTheAnnex.java` | ON_OPPONENT_CASTS_SPELL CounterUnlessPaysEffect(1) — auto-targets the triggering spell. Also has ON_OPENING_HAND_REVEAL MayEffect(RegisterDelayedCounterTriggerEffect(1)) for Chancellor-style delayed counter trigger on first opponent spell |
| Opponent creature spell punisher (pay or life) | `i/IsolationCell.java` | ON_OPPONENT_CASTS_SPELL LoseLifeUnlessPaysEffect(2, 2, CardTypePredicate(CREATURE)) — filtered to creature spells only, pay mana or lose life |
| Opponent color spell +1/+1 counter (may) | `m/MoldAdder.java` | ON_OPPONENT_CASTS_SPELL MayEffect(PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect(BLUE+BLACK, 1, false)) — may put +1/+1 counter when opponent casts blue or black spell |
| Opponent spell reveal-creature-to-battlefield | `l/LurkingPredators.java` | ON_OPPONENT_CASTS_SPELL RevealTopCardCreatureToBattlefieldOrMayBottomEffect — reveal top card, creature goes to battlefield, non-creature may go to bottom |
| Opponent land enters (punisher) | `t/TunnelIgnus.java` | ON_OPPONENT_LAND_ENTERS_BATTLEFIELD PermanentEnteredThisTurnConditionalEffect(DealDamageToTargetPlayerEffect(3), CardTypePredicate(LAND), 2) — conditional wrapper checks 2+ lands entered this turn |
| Imprint ETB exile land + opponent land name match | `i/InvaderParasite.java` | ON_ENTER_BATTLEFIELD ExileTargetPermanentAndImprintEffect + ON_OPPONENT_LAND_ENTERS_BATTLEFIELD ImprintedCardNameMatchesEnteringPermanentConditionalEffect(DealDamageToTargetPlayerEffect(2)) — exile target land on ETB, deal 2 damage when opponent plays land with same name |
| Land tap trigger | `m/Manabarbs.java` | ON_ANY_PLAYER_TAPS_LAND DealDamageOnLandTapEffect |
| Land tap mana doubling + opponent untap lock | `v/VorinclexVoiceOfHunger.java` | ON_ANY_PLAYER_TAPS_LAND AddOneOfEachManaTypeProducedByLandEffect + OpponentTappedLandDoesntUntapEffect — doubles controller's land mana, opponent lands skip next untap step |
| End step self-destruct | `s/SparkElemental.java` | END_STEP_TRIGGERED SacrificeSelfEffect |
| End step morbid destroy | `r/ReaperFromTheAbyss.java` | END_STEP_TRIGGERED MorbidConditionalEffect(DestroyTargetPermanentEffect()) + target(PermanentPredicateTargetFilter(non-Demon creature)) — intervening-if morbid check at trigger time, targeting via `pendingEndStepTriggerTargets` |
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
| Subtype lord (all) | `g/GoblinKing.java` | STATIC StaticBoostEffect with PermanentHasAnySubtypePredicate filter, ALL_CREATURES scope |
| Subtype lord (own) + keyword | `k/KnightExemplar.java` | STATIC StaticBoostEffect(1, 1, Set.of(INDESTRUCTIBLE), OWN_CREATURES, PermanentHasAnySubtypePredicate) — +1/+1 and indestructible to other Knights you control |
| Anthem (all own) | `g/GloriousAnthem.java` | STATIC StaticBoostEffect with OWN_CREATURES scope, no filter |
| Supertype lord (own) | `a/ArvadTheCursed.java` | STATIC StaticBoostEffect(2, 2, OWN_CREATURES, PermanentHasSupertypePredicate(LEGENDARY)) — +2/+2 to other legendary creatures you control |
| Color boost/debuff | `a/AscendantEvincar.java` | STATIC StaticBoostEffect with PermanentColorInPredicate / PermanentNotPredicate filter |
| Own boost + opponent debuff | `e/EleshNornGrandCenobite.java` | STATIC StaticBoostEffect(2, 2, OWN_CREATURES) + StaticBoostEffect(-2, -2, OPPONENT_CREATURES) |
| Color keyword lord | `b/BellowingTanglewurm.java` | STATIC GrantKeywordEffect with PermanentColorInPredicate filter, OWN_CREATURES scope |
| Keyword lord + spell trigger | `h/HandOfThePraetors.java` | STATIC StaticBoostEffect with PermanentHasKeywordPredicate(INFECT) filter, OWN_CREATURES scope + ON_ANY_PLAYER_CASTS_SPELL GiveTargetPlayerPoisonCountersEffect with CardAllOfPredicate(CREATURE, INFECT) |
| Attachment self-buff | `c/ChampionOfTheFlame.java` | STATIC BoostSelfPerAttachmentEffect(2, 2, true, true) — +2/+2 for each Aura and Equipment attached. Use BoostSelfPerEquipmentAttachedEffect for Equipment-only (Goblin Gaveleer) |
| Tribal combat trigger (subtype counter lord) | `r/RakishHeir.java` | ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER PutCountersOnDamageDealerEffect(1,1,1, PermanentHasSubtypePredicate(VAMPIRE)) — when a Vampire you control deals combat damage to a player, put +1/+1 counter on it |
| Choose subtype + grant to own | `x/Xenograft.java` | ON_ENTER_BATTLEFIELD ChooseSubtypeOnEnterEffect + STATIC GrantChosenSubtypeToOwnCreaturesEffect |
| Shared-type pump | `c/CoatOfArms.java` | STATIC BoostBySharedCreatureTypeEffect |
| Can't block | `s/SpinelessThug.java` | STATIC CantBlockEffect |
| Must attack | `b/BloodrockCyclops.java` | STATIC MustAttackEffect |
| Evasion (blocked only by) | `e/ElvenRiders.java` | STATIC CanBeBlockedOnlyByFilterEffect |
| Block limit | `s/StalkingTiger.java` | STATIC CanBeBlockedByAtMostNCreaturesEffect |
| Unblockable | `p/PhantomWarrior.java` | STATIC CantBeBlockedEffect |
| Conditional unblockable | `s/ScrapdiverSerpent.java` | STATIC CantBeBlockedIfDefenderControlsMatchingPermanentEffect |
| Attack restriction (defender) | `s/SeaMonster.java` | STATIC CantAttackUnlessDefenderControlsMatchingPermanentEffect |
| Attack restriction (battlefield count) | `h/HarborSerpent.java` | STATIC CantAttackUnlessBattlefieldHasMatchingPermanentCountEffect(predicate, 5, desc) — can't attack unless N+ matching permanents across all battlefields |
| Attack restriction (opponent damage) | `b/BloodcrazedGoblin.java` | STATIC CantAttackUnlessOpponentDealtDamageThisTurnEffect — can't attack unless an opponent was dealt damage this turn |
| Block restriction | `c/CloudElemental.java` | STATIC CanBlockOnlyIfAttackerMatchesPredicateEffect |
| Protection from colors | `p/PaladinEnVec.java` | STATIC ProtectionFromColorsEffect |
| Prevent all damage to self | `c/ChoMannoRevolutionary.java` | STATIC PreventAllDamageEffect |
| Can't lose game | `p/PlatinumAngel.java` | STATIC CantLoseGameEffect |
| Can't lose from life + damage as infect | `p/PhyrexianUnlife.java` | STATIC CantLoseGameFromLifeEffect + DamageDealtAsInfectBelowZeroLifeEffect |
| Can't lose + life gain draw + life loss exile + LTB lose | `l/LichsMastery.java` | STATIC CantLoseGameEffect + GrantKeywordEffect(HEXPROOF, SELF), ON_CONTROLLER_GAINS_LIFE DrawCardsEqualToLifeGainedEffect, ON_CONTROLLER_LOSES_LIFE ExileForEachLifeLostEffect, ON_SELF_LEAVES_BATTLEFIELD ControllerLosesGameOnLeavesEffect |
| Controller shroud | `t/TrueBeliever.java` | STATIC GrantControllerShroudEffect |
| Can't cast type | `s/SteelGolem.java` | STATIC CantCastSpellTypeEffect |
| Limit spells | `r/RuleOfLaw.java` | STATIC LimitSpellsPerTurnEffect |
| Tax attackers | `w/WindbornMuse.java` | STATIC RequirePaymentToAttackEffect |
| Tax opponent spells | `a/AuraOfSilence.java` | STATIC IncreaseOpponentCastCostEffect |
| Enters tapped | `r/RootMaze.java` | STATIC EnterPermanentsOfTypesTappedEffect |
| Opponent creatures enter tapped + haste lord | `u/UrabraskTheHidden.java` | STATIC GrantKeywordEffect(HASTE, OWN_CREATURES) + EnterPermanentsOfTypesTappedEffect(CREATURE, opponentsOnly=true) |
| P/T = lands | `m/MolimoMaroSorcerer.java` | STATIC PowerToughnessEqualToControlledLandCountEffect |
| P/T = creatures | `s/ScionOfTheWild.java` | STATIC PowerToughnessEqualToControlledCreatureCountEffect |
| P/T = subtype | `n/Nightmare.java` | STATIC PowerToughnessEqualToControlledSubtypeCountEffect |
| P/T = GY creatures | `m/Mortivore.java` | STATIC PowerToughnessEqualToCreatureCardsInAllGraveyardsEffect |
| P/T = hand size + draw trigger | `p/PsychosisCrawler.java` | STATIC PowerToughnessEqualToCardsInHandEffect + ON_CONTROLLER_DRAWS EachOpponentLosesLifeEffect |
| Gain GY creature abilities | `n/NecroticOoze.java` | STATIC GainActivatedAbilitiesOfCreatureCardsInAllGraveyardsEffect — selfOnly, gains all activated abilities of all creature cards in all graveyards |
| +1/+1 per same name | `r/RelentlessRats.java` | STATIC BoostByOtherCreaturesWithSameNameEffect |
| +1/+0 per other subtype you control | `r/RatColony.java` | STATIC BoostSelfPerOtherControlledSubtypeEffect(RAT, 1, 0) |
| Cost reduction | `a/AvatarOfMight.java` | STATIC ReduceOwnCastCostIfOpponentControlsMoreCreaturesEffect |
| Subtype cost reduction | `d/DanithaCapashenParagon.java` | STATIC ReduceOwnCastCostForSubtypeEffect(Set.of(AURA, EQUIPMENT), 1) — from battlefield permanent |
| Creature mana only | `m/MyrSuperion.java` | setRequiresCreatureMana(true) — can only be cast with mana produced by creatures |
| No max hand size | `s/Spellbook.java` | STATIC NoMaximumHandSizeEffect |
| Double damage | `f/FurnaceOfRath.java` | STATIC DoubleDamageEffect |
| Play lands from GY | `c/CrucibleOfWorlds.java` | STATIC PlayLandsFromGraveyardEffect |
| Draw replacement | `a/Abundance.java` | STATIC AbundanceDrawReplacementEffect |
| Grant flash to spell type | `s/ShimmerMyr.java` | STATIC GrantFlashToCardTypeEffect(ARTIFACT) — controller may cast artifact spells as though they had flash |
| Grant flash to all spells + leyline | `l/LeylineOfAnticipation.java` | ON_OPENING_HAND_REVEAL MayEffect(LeylineStartOnBattlefieldEffect) + STATIC GrantFlashToCardTypeEffect(null) — may start on battlefield from opening hand, grants flash to all spells |
| Metalcraft keyword | `a/AuriokEdgewright.java` | STATIC MetalcraftConditionalEffect(GrantKeywordEffect(DOUBLE_STRIKE, SELF)) |
| Metalcraft keyword + boost | `a/AuriokSunchaser.java` | STATIC MetalcraftConditionalEffect(GrantKeywordEffect) + MetalcraftConditionalEffect(StaticBoostEffect) |
| Metalcraft boost only | `c/CarapaceForger.java` | STATIC MetalcraftConditionalEffect(StaticBoostEffect(2, 2, SELF)) |
| Metalcraft boost + ignore defender | `s/SpireSerpent.java` | STATIC MetalcraftConditionalEffect(StaticBoostEffect) + MetalcraftConditionalEffect(CanAttackAsThoughNoDefenderEffect) |
| Metalcraft become creature | `r/RustedRelic.java` | STATIC MetalcraftConditionalEffect(AnimateSelfWithStatsEffect(5, 5, [GOLEM], [])) — noncreature becomes creature with fixed P/T and subtypes |
| Metalcraft burn spell | `g/GalvanicBlast.java` | SPELL MetalcraftReplacementEffect(DealDamageToAnyTargetEffect(2), DealDamageToAnyTargetEffect(4)) — picks base/upgrade at resolution |
| Morbid burn spell | `b/BrimstoneVolley.java` | SPELL MorbidReplacementEffect(DealDamageToAnyTargetEffect(3), DealDamageToAnyTargetEffect(5)) — picks base/upgrade at resolution based on creature death this turn |
| Morbid land search | `c/CaravanVigil.java` | SPELL MorbidReplacementEffect(SearchLibraryForBasicLandToHandEffect(), SearchLibraryForCardTypesToBattlefieldEffect(LAND, basic, untapped)) — search for basic land to hand, or to battlefield untapped if morbid |
| Morbid ETB counters | `f/FesterhideBoar.java` | ON_ENTER_BATTLEFIELD MorbidConditionalEffect(PutCountersOnSourceEffect(1, 1, 2)) — enters with +1/+1 counters if a creature died this turn |
| Metalcraft damage + can't block | `c/ConcussiveBolt.java` | SPELL DealDamageToTargetPlayerEffect(4) + MetalcraftConditionalEffect(TargetPlayerCreaturesCantBlockThisTurnEffect) — damage always, metalcraft adds mass can't-block on target player's creatures |
| Life threshold boost + keyword | `s/SerraAscendant.java` | STATIC ControllerLifeThresholdConditionalEffect(30, StaticBoostEffect(5, 5, [FLYING], SELF)) — +5/+5 and flying as long as controller has 30+ life |
| Graveyard card threshold boost + keyword | `g/GhituLavarunner.java` | STATIC ControllerGraveyardCardThresholdConditionalEffect(2, instant/sorcery filter, StaticBoostEffect(1, 0, [HASTE], SELF)) — +1/+0 and haste as long as 2+ instant/sorcery cards in controller's graveyard |

## Auras

| Pattern | Reference | Notes |
|---------|-----------|-------|
| Lockdown (can't attack/block) | `p/Pacifism.java` | STATIC EnchantedCreatureCantAttackOrBlockEffect |
| Subtype-conditional aura | `b/BondsOfFaith.java` | STATIC EnchantedCreatureSubtypeConditionalEffect(HUMAN, StaticBoostEffect(2, 2, GrantScope.ENCHANTED_CREATURE), EnchantedCreatureCantAttackOrBlockEffect()) — composes existing effects, +2/+2 if Human, can't attack/block otherwise |
| Lockdown (can't attack) + self-bounce | `f/ForcedWorship.java` | STATIC EnchantedCreatureCantAttackEffect + activated ReturnSelfToHandEffect |
| Full lockdown (can't attack/block/activate) | `a/Arrest.java` | STATIC EnchantedCreatureCantAttackOrBlockEffect + EnchantedCreatureCantActivateAbilitiesEffect |
| P/T override + ability strip aura | `d/DeepFreeze.java` | STATIC SetBasePowerToughnessStaticEffect(0, 4, ENCHANTED_CREATURE) + GrantKeywordEffect(DEFENDER, ENCHANTED_CREATURE) + LosesAllAbilitiesEffect(ENCHANTED_CREATURE) + GrantColorEffect(BLUE, ENCHANTED_CREATURE) + GrantSubtypeEffect(WALL, ENCHANTED_CREATURE) — sets base P/T, grants defender, strips all original abilities, adds color and type |
| Lockdown + self-destruct on target | `i/IceCage.java` | STATIC EnchantedCreatureCantAttackOrBlockEffect + EnchantedCreatureCantActivateAbilitiesEffect + ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY DestroySourcePermanentEffect |
| Doesn't untap | `d/Dehydration.java` | STATIC EnchantedCreatureDoesntUntapEffect |
| Static boost | `h/HolyStrength.java` | STATIC StaticBoostEffect(X, Y, GrantScope.ENCHANTED_CREATURE) |
| Boost + keyword | `s/SerrasEmbrace.java` | Boost + GrantKeywordEffect(ENCHANTED_CREATURE) |
| Keyword + combat damage prevention | `g/GhostlyPossession.java` | GrantKeywordEffect(FLYING) + PreventAllCombatDamageToAndByEnchantedCreatureEffect (non-combat damage still goes through) |
| Boost per subtype | `b/BlanchwoodArmor.java` | STATIC BoostCreaturePerControlledSubtypeEffect |
| Control enchanted | `p/Persuasion.java` | STATIC ControlEnchantedCreatureEffect |
| Grant activated ability | `a/ArcaneTeachings.java` | GrantActivatedAbilityEffect with GrantScope.ENCHANTED_CREATURE |
| Redirect damage to creature | `p/Pariah.java` | STATIC RedirectPlayerDamageToEnchantedCreatureEffect |
| Prevent X + redirect to player | `v/VengefulArchon.java` | Activated {X}: PreventXDamageToControllerAndRedirectToTargetPlayerEffect — prevent next X damage to you, source deals that much to target player |
| Enchanted land mana | `o/Overgrowth.java` | ON_ANY_PLAYER_TAPS_LAND AddManaOnEnchantedLandTapEffect |
| Enchanted land becomes basic type | `e/EvilPresence.java` | STATIC EnchantedPermanentBecomesTypeEffect(SWAMP) — land loses all land types/abilities, becomes the new basic land type |
| Aura + self-bounce ability | `s/ShimmeringWings.java` | STATIC keyword + activated ReturnSelfToHandEffect |
| Grant keyword + upkeep counter + death return | `g/GlisteningOil.java` | STATIC GrantKeywordEffect(INFECT, ENCHANTED_CREATURE) + UPKEEP_TRIGGERED PutMinusOneMinusOneCounterOnEnchantedCreatureEffect + ON_DEATH ReturnCardFromGraveyardEffect.builder().destination(HAND).filter(CardIsSelfPredicate).build() |
| Upkeep +1/+1 counter + sacrifice on combat | `p/PrimalCocoon.java` | UPKEEP_TRIGGERED PutPlusOnePlusOneCounterOnEnchantedCreatureEffect + ON_ATTACK SacrificeSelfEffect + ON_BLOCK SacrificeSelfEffect |
| Doesn't untap + enchanted controller upkeep life loss | `n/NumbingDose.java` | STATIC AttachedCreatureDoesntUntapEffect + ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED EnchantedCreatureControllerLosesLifeEffect(1) — enchants artifact or creature, uses PermanentAnyOfPredicate target filter |
| Enchanted permanent death trigger (gain life) | `v/ViridianHarvest.java` | ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD GainLifeEffect(6) — enchants artifact, aura controller gains life when enchanted permanent is put into graveyard |
| Enchanted permanent LTB trigger (conditional draw) | `c/CuratorsWard.java` | STATIC GrantKeywordEffect(HEXPROOF, ENCHANTED_CREATURE) + ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD EnchantedPermanentLeavesConditionalEffect(CardIsHistoricPredicate, DrawCardEffect(2)) — enchants any permanent, grants hexproof, draws 2 when enchanted permanent leaves battlefield if historic |
| Curse (enchant player + static debuff) | `c/CurseOfDeathsHold.java` | STATIC StaticBoostEffect(-1, -1, GrantScope.ENCHANTED_PLAYER_CREATURES) — enchant player aura (auto-detected from CURSE subtype via `isEnchantPlayer()`), creatures enchanted player controls get -1/-1 |
| Curse (enchant player + combat trigger) | `c/CurseOfStalkedPrey.java` | ON_COMBAT_DAMAGE_TO_PLAYER PutCountersOnSourceEffect(1,1,1) — enchants player (auto-detected from CURSE subtype), whenever a creature deals combat damage to enchanted player, put +1/+1 counter on that creature. CombatDamageService checks curses attached to defending player |
| Curse (enchant player + upkeep damage) | `c/CurseOfThePiercedHeart.java` | ENCHANTED_PLAYER_UPKEEP_TRIGGERED DealDamageToEnchantedPlayerEffect(1) — enchant player curse; at enchanted player's upkeep, deals 1 damage to that player |
| Curse (enchant player + upkeep graveyard exile) | `c/CurseOfOblivion.java` | ENCHANTED_PLAYER_UPKEEP_TRIGGERED ExileCardsFromOwnGraveyardEffect(2) — enchant player curse; at enchanted player's upkeep, that player exiles 2 cards from their graveyard |
| Curse (enchant player + upkeep mill) | `c/CurseOfTheBloodyTome.java` | ENCHANTED_PLAYER_UPKEEP_TRIGGERED MillTargetPlayerEffect(2) — enchant player curse; at enchanted player's upkeep, that player mills 2 cards |

## Artifacts

| Pattern | Reference | Notes |
|---------|-----------|-------|
| Targeted ETB + activated ability | `c/ContagionClasp.java` | ON_ENTER_BATTLEFIELD PutMinusOneMinusOneCounterOnTargetCreatureEffect + tap+mana ProliferateEffect activated ability |
| Sac creature for counter + sac self for draw | `c/CullingDais.java` | Two abilities: tap+SacrificeCreatureCost+PutChargeCounterOnSelfEffect, mana+SacrificeSelfCost+DrawCardsEqualToChargeCountersOnSourceEffect |
| ETB + death draw (simple) | `i/IchorWellspring.java` | ON_ENTER_BATTLEFIELD DrawCardEffect + ON_DEATH DrawCardEffect — draws on ETB and when destroyed/sacrificed |
| Spellbomb (sac for effect + may-pay draw) | `f/FlightSpellbomb.java` | Tap+SacrificeSelfCost+GrantKeywordEffect(TARGET) ability + ON_DEATH MayPayManaEffect("{U}", DrawCardEffect(1)) — Spellbomb cycle pattern |
| Charge counter trigger + activated token | `g/GolemFoundry.java` | MayEffect(PutChargeCounterOnSelfOnArtifactCastEffect) on ON_ANY_PLAYER_CASTS_SPELL + activated RemoveChargeCountersFromSourceCost(3) + CreateCreatureTokenEffect |
| Enters with fixed charge counters + tap-remove ability | `n/NecrogenCenser.java` | EnterWithFixedChargeCountersEffect(2) + tap+RemoveChargeCountersFromSourceCost(1)+TargetPlayerLosesLifeAndControllerGainsLifeEffect |
| Study counter accumulation + sacrifice for mass reanimate | `g/GrimoireOfTheDead.java` | Ability 1: tap+{1}+DiscardCardTypeCost(null)+PutCounterOnSelfEffect(STUDY). Ability 2: tap+RemoveCounterFromSourceCost(3,STUDY)+SacrificeSelfCost+ReturnCardFromGraveyardEffect(returnAll, ALL_GRAVEYARDS, grantColor BLACK, grantSubtype ZOMBIE) |
| Dual sacrifice abilities (damage + counter removal) | `g/GremlinMine.java` | Two abilities: tap+{1}+SacrificeSelfCost+DealDamageToTargetCreatureEffect(4) with artifact creature filter + tap+{1}+SacrificeSelfCost+RemoveChargeCountersFromTargetPermanentEffect(4) with noncreature artifact filter |
| Exile self cost + exile target creature | `b/BrittleEffigy.java` | tap+{4}+ExileSelfCost+ExileTargetPermanentEffect with PermanentIsCreaturePredicate filter. ExileSelfCost exiles self as cost (not sacrifice) |
| Type-changing tap ability | `l/LiquimetalCoating.java` | Tap: AddCardTypeToTargetPermanentEffect(ARTIFACT) — target permanent becomes artifact until end of turn |
| Hand-imprint + X-cost token | `p/PrototypePortal.java` | ON_ENTER_BATTLEFIELD MayEffect(ExileFromHandToImprintEffect(CardTypePredicate(ARTIFACT))) + activated {X}+tap CreateTokenCopyOfImprintedCardEffect(false, false) — imprint artifact from hand on ETB, pay X (imprinted card's mana cost) to create permanent token copy |
| Graveyard exile + pay cost + token copy | `b/BackFromTheBrink.java` | Activated sorcery-speed ExileCardFromGraveyardCost(CREATURE, true, true) + CreateTokenCopyOfExiledCostCardEffect — exile creature from graveyard, pay its mana cost, create token copy. Dynamic mana cost from exiled card |
| Hand-imprint + cost reduction | `s/SemblanceAnvil.java` | ON_ENTER_BATTLEFIELD MayEffect(ExileFromHandToImprintEffect(CardNotPredicate(CardTypePredicate(LAND)))) + STATIC ReduceOwnCastCostForSharedCardTypeWithImprintEffect(2) — imprint nonland card from hand on ETB, spells sharing card type with imprinted card cost {2} less |
| Static + upkeep trigger | `v/VensersJournal.java` | STATIC NoMaximumHandSizeEffect + UPKEEP_TRIGGERED GainLifePerCardsInHandEffect — no max hand size + gain life equal to hand size each upkeep |
| Sacrifice creature + search by MV | `b/BirthingPod.java` | Tap+mana(Phyrexian)+SacrificeCreatureCost(true)+SearchLibraryForCreatureWithExactMVToBattlefieldEffect(1) — sorcery speed, sacrifice creature to tutor creature with MV+1 to battlefield. `trackSacrificedManaValue=true` stores sacrificed MV in xValue |
| Choose color + static boost + mana bonus | `c/CagedSun.java` | ON_ENTER_BATTLEFIELD ChooseColorOnEnterEffect + STATIC BoostCreaturesOfChosenColorEffect(1,1) + ON_ANY_PLAYER_TAPS_LAND AddExtraManaOfChosenColorOnLandTapEffect — choose color on enter, creatures of chosen color get +1/+1, lands tapped for chosen color add extra mana |
| Shrine (upkeep + spell trigger charge + sac-for-mana) | `s/ShrineOfBoundlessGrowth.java` | UPKEEP_TRIGGERED PutChargeCounterOnSelfEffect + ON_CONTROLLER_CASTS_SPELL SpellCastTriggerEffect(CardColorPredicate) + tap+SacrificeSelfCost+AddColorlessManaPerChargeCounterOnSourceEffect — charge counters on upkeep and colored spell cast, sacrifice for mana |
| Upkeep + spell-cast charge counters + sac damage | `s/ShrineOfBurningRage.java` | UPKEEP_TRIGGERED PutChargeCounterOnSelfEffect + ON_CONTROLLER_CASTS_SPELL SpellCastTriggerEffect(CardColorPredicate(RED)) + activated {3}+tap+SacrificeSelfCost+DealDamageToAnyTargetEqualToChargeCountersOnSourceEffect |
| Shrine (upkeep + spell trigger charge + sac-for-library-pick) | `s/ShrineOfPiercingVision.java` | UPKEEP_TRIGGERED PutChargeCounterOnSelfEffect + ON_CONTROLLER_CASTS_SPELL SpellCastTriggerEffect(CardColorPredicate(BLUE)) + tap+SacrificeSelfCost+LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffect — charge counters on upkeep and blue spell cast, sacrifice to look at top X and pick one to hand |
| Upkeep random graveyard return + spell-cast self-boost | `c/CharmbreakerDevils.java` | UPKEEP_TRIGGERED ReturnCardFromGraveyardEffect.builder().destination(HAND).filter(instant/sorcery filter).returnAtRandom(true).build() + ON_CONTROLLER_CASTS_SPELL SpellCastTriggerEffect(instant/sorcery filter, BoostSelfEffect(4,0)) — return random instant/sorcery from graveyard at upkeep, +4/+0 when casting instant/sorcery |
| Multi-target player ability (exchange life) | `s/SoulConduit.java` | Activated {6}+tap ExchangeTargetPlayersLifeTotalsEffect with multi-target PlayerPredicateTargetFilter(ANY) — two target players exchange life totals |
| Untap own artifacts on opponent's untap | `u/UnwindingClock.java` | STATIC UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect(UNTAP, PermanentIsArtifactPredicate) — filtered variant of Seedborn Muse (which uses null filter for all permanents) |
| Coin flip activated ability | `s/SorcerersStrongbox.java` | Tap+{2} FlipCoinWinEffect(SacrificeSelfAndDrawCardsEffect(3)) — flip a coin, if you win sacrifice self and draw 3 |
| Token creation + tap-X-subtype destruction | `a/AryelKnightOfWindgrace.java` | Ability 1: tap+{2}{W} CreateCreatureTokenEffect(Knight 2/2 white vigilance). Ability 2: tap+{B}+TapXPermanentsCost(Knight, excludeSource)+DestroyTargetPermanentEffect with PermanentPowerAtMostXPredicate target filter — tap X untapped Knights to destroy creature with power X or less |

## Equipment

| Pattern | Reference | Notes |
|---------|-----------|-------|
| Simple boost equip | `l/LeoninScimitar.java` | STATIC StaticBoostEffect(X, Y, GrantScope.EQUIPPED_CREATURE) + EquipActivatedAbility |
| Boost + keywords equip | `l/LoxodonWarhammer.java` | Boost + GrantKeywordEffect(EQUIPPED_CREATURE) + equip |
| Boost + conditional keyword equip | `j/JoustingLance.java` | STATIC StaticBoostEffect(2, 0, EQUIPPED_CREATURE) + ControllerTurnConditionalEffect(GrantKeywordEffect(FIRST_STRIKE, EQUIPPED_CREATURE)) + equip — keyword only applies during controller's turn |
| Boost + can't block equip | `c/CopperCarapace.java` | STATIC StaticBoostEffect(X, Y, GrantScope.EQUIPPED_CREATURE) + CantBlockEffect + equip |
| ETB attach + sacrifice equip | `p/PistonSledge.java` | ON_ENTER_BATTLEFIELD AttachSourceEquipmentToTargetCreatureEffect + STATIC StaticBoostEffect(X, Y, GrantScope.EQUIPPED_CREATURE) + equip with SacrificeArtifactCost (no mana). Uses ControlledPermanentPredicateTargetFilter on card for ETB targeting |
| Living weapon equip | `s/Strandwalker.java` | ON_ENTER_BATTLEFIELD LivingWeaponEffect + STATIC StaticBoostEffect(X, Y, GrantScope.EQUIPPED_CREATURE) + GrantKeywordEffect(EQUIPPED_CREATURE) + equip |
| Living weapon + per-subtype boost equip | `l/Lashwrithe.java` | ON_ENTER_BATTLEFIELD LivingWeaponEffect + STATIC BoostCreaturePerControlledSubtypeEffect(SWAMP, 1, 1, GrantScope.EQUIPPED_CREATURE) + Phyrexian mana equip |
| Boost + color/subtype + death trigger equip | `n/NimDeathmantle.java` | STATIC StaticBoostEffect(X, Y, GrantScope.EQUIPPED_CREATURE) + GrantKeywordEffect(EQUIPPED_CREATURE) + GrantColorEffect(BLACK, EQUIPPED_CREATURE) + GrantSubtypeEffect(ZOMBIE, EQUIPPED_CREATURE) + ON_ANY_NONTOKEN_CREATURE_DIES MayPayManaEffect("{4}", ReturnDyingCreatureToBattlefieldAndAttachSourceEffect) + equip |
| Keyword + graveyard-count boost equip | `r/RunechantersPike.java` | STATIC GrantKeywordEffect(FIRST_STRIKE, EQUIPPED_CREATURE) + BoostCreaturePerCardsInControllerGraveyardEffect(instant+sorcery filter, 1, 0, EQUIPPED_CREATURE) + equip — +X/+0 where X = instants+sorceries in controller's graveyard |
| Imprint + land-name boost equip | `s/StrataScythe.java` | ON_ENTER_BATTLEFIELD SearchLibraryForCardTypeToExileAndImprintEffect(LAND) + STATIC BoostCreaturePerMatchingLandNameEffect(1, 1, GrantScope.EQUIPPED_CREATURE) + equip — imprint land from library on ETB, equipped creature gets +1/+1 per matching land on all battlefields |
| Boost + equipped creature death trigger equip | `s/SylvokLifestaff.java` | STATIC StaticBoostEffect(1, 0, GrantScope.EQUIPPED_CREATURE) + ON_EQUIPPED_CREATURE_DIES GainLifeEffect(3) + equip — triggers when the creature it's attached to dies |
| Boost + keywords + death reattach equip | `f/ForebearsBlade.java` | STATIC StaticBoostEffect(3, 0, EQUIPPED_CREATURE) + GrantKeywordEffect(VIGILANCE, EQUIPPED_CREATURE) + GrantKeywordEffect(TRAMPLE, EQUIPPED_CREATURE) + ON_EQUIPPED_CREATURE_DIES AttachSourceEquipmentToTargetCreatureEffect + setCastTimeTargetFilter(ControlledPermanentPredicateTargetFilter) + equip. Targeted death trigger: when equipped creature dies, controller chooses a creature to reattach to |
| Grant ability + doesn't untap equip | `h/HeavyArbalest.java` | STATIC EquippedCreatureDoesntUntapEffect + GrantActivatedAbilityEffect(EQUIPPED_CREATURE) + equip |
| Per-blocker trigger equip | `i/InfiltrationLens.java` | ON_BECOMES_BLOCKED MayEffect(DrawCardEffect(2)) + `TriggerMode.PER_BLOCKER` + equip. Use `addEffect(slot, effect, TriggerMode.PER_BLOCKER)` for "becomes blocked by a creature" triggers that fire once per blocker |
| Becomes-target trigger equip | `l/LivewireLash.java` | ON_BECOMES_TARGET_OF_SPELL DealDamageToAnyTargetEffect(2) + STATIC StaticBoostEffect(2, 0, GrantScope.EQUIPPED_CREATURE) + equip. Grants triggered ability to equipped creature: when it becomes the target of a spell, deal 2 damage to any target |
| Boost + grant card type equip | `s/SilverskinArmor.java` | STATIC StaticBoostEffect(1, 1, GrantScope.EQUIPPED_CREATURE) + GrantCardTypeEffect(ARTIFACT, EQUIPPED_CREATURE) + equip. Makes equipped creature an artifact in addition to its other types (counts for metalcraft, artifact targeting, etc.) |
| Sword cycle (boost + protection + combat trigger) | `s/SwordOfWarAndPeace.java` | STATIC StaticBoostEffect(2, 2, GrantScope.EQUIPPED_CREATURE) + ProtectionFromColorsEffect(RED, WHITE) + ON_COMBAT_DAMAGE_TO_PLAYER DealDamageToTargetPlayerByHandSizeEffect + GainLifePerCardsInHandEffect + equip. Also: `SwordOfFeastAndFamine` (discard + untap lands), `SwordOfBodyAndMind` (token + mill) |
| Evasion + sacrifice-equip-to-deal-damage | `b/BlazingTorch.java` | STATIC CanBeBlockedOnlyByFilterEffect(PermanentNotPredicate(vampires/zombies)) + GrantActivatedAbilityEffect(EQUIPPED_CREATURE) with SacrificeSourceEquipmentCost + DealDamageToAnyTargetEffect + equip |
| Double combat damage dealt/received equip | `i/InquisitorsFlail.java` | STATIC DoubleEquippedCreatureCombatDamageEffect + equip. Doubles combat damage dealt by and received by equipped creature |
| Per-card-type boost + dual equip cost | `b/BlackbladeReforged.java` | STATIC BoostCreaturePerControlledCardTypeEffect(LAND, 1, 1, EQUIPPED_CREATURE) + equip legendary creature {3} (ActivatedAbility with PermanentAllOfPredicate(creature + legendary)) + EquipActivatedAbility("{7}") |
| Combat-triggered token copy equip | `h/HelmOfTheHost.java` | BEGINNING_OF_COMBAT_TRIGGERED CreateTokenCopyOfEquippedCreatureEffect(true, true) + EquipActivatedAbility("{5}") — at beginning of combat on your turn, create a token copy of equipped creature (not legendary, gains haste) |
| Boost + subtype combat destroy equip | `w/WoodenStake.java` | STATIC StaticBoostEffect(1, 0, EQUIPPED_CREATURE) + ON_BLOCK DestroySubtypeCombatOpponentEffect(VAMPIRE, true) + ON_BECOMES_BLOCKED DestroySubtypeCombatOpponentEffect(VAMPIRE, true) PER_BLOCKER + equip. Destroys Vampires that block or are blocked by equipped creature (can't regenerate) |

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
| Sacrifice self + damage player/pw | `v/VulshokReplica.java` | SacrificeSelfCost + DealDamageToAnyTargetEffect + PermanentIsPlaneswalkerPredicate filter (restricts to players + planeswalkers) |
| Sacrifice self + choose source prevention | `a/AuriokReplica.java` | SacrificeSelfCost + PreventAllDamageFromChosenSourceEffect (prompts permanent choice on resolution) |
| Target prevention from chosen source + life gain | `h/HealingGrace.java` | PreventDamageToTargetFromChosenSourceEffect(3) + GainLifeEffect(3) — target chosen on cast, source chosen on resolution |
| Sacrifice subtype for effect | `s/SiegeGangCommander.java` | SacrificeSubtypeCreatureCost + DealDamageToAnyTargetEffect |
| Sacrifice artifact for effect | `b/BarrageOgre.java` | SacrificeArtifactCost + DealDamageToAnyTargetEffect (tap + sac artifact) |
| Sacrifice multiple permanents for tutor | `k/KuldothaForgemaster.java` | SacrificeMultiplePermanentsCost(3, PermanentIsArtifactPredicate) + SearchLibraryForCardTypesToBattlefieldEffect(ARTIFACT) (tap + sac 3 artifacts) |
| Tap to +1/+1 counters on controlled filtered permanents | `s/SteelOverseer.java` | `(true, null, PutPlusOnePlusOneCounterOnEachControlledPermanentEffect(PermanentAllOfPredicate(artifact+creature)))` — no target, affects all matching permanents you control |
| Regenerate (self) | `d/DrudgeSkeletons.java` | `(false, "{B}", RegenerateEffect, false)` |
| Regenerate (target creature) | `a/Asceticism.java` | `RegenerateEffect(true)` + PermanentPredicateTargetFilter |
| Static effect grant (own creatures) | `a/Asceticism.java` | `GrantEffectEffect(CantBeTargetOfSpellsOrAbilitiesEffect, GrantScope.OWN_CREATURES)` |
| Create token | `d/DragonRoost.java` | CreateCreatureTokenEffect |
| Mill target | `m/Millstone.java` | `(true, "{2}", MillTargetPlayerEffect, true)` |
| Mana dork (tap for color) | `b/BirdsOfParadise.java` | `(true, null, AwardAnyColorManaEffect, false)` |
| Mana rock (tap for N of any color) | `g/GildedLotus.java` | `(true, null, AwardAnyColorManaEffect(3), false)` |
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
| Planeswalker with opponent reveal choice + silver counter exile + artifact-scaling token | `k/KarnScionOfUrza.java` | +1 KarnScionRevealTwoOpponentChoosesEffect, -1 KarnScionReturnSilverCounterCardEffect, -2 CreateCreatureTokenEffect with STATIC BoostSelfPerControlledPermanentEffect(1,1,PermanentIsArtifactPredicate) via tokenEffects map |
| Variable loyalty (-X) | `c/ChandraNalaar.java` | `ActivatedAbility.variableLoyaltyAbility(effects, desc, filter)` — loyalty cost is -X chosen by player, X stored in xValue |
| Loyalty + mana + player damage | `c/ChandraBoldPyromancer.java` | +1 with AwardManaEffect + DealDamageToTargetPlayerEffect, -3 DealDamageToTargetCreatureOrPlaneswalkerEffect, -7 DealDamageToTargetPlayerEffect + DealDamageToAllCreaturesAndPlaneswalkersTargetControlsEffect |
| Multi-target tap ability (equip mover) | `b/BrassSquire.java` | `ActivatedAbility(true, null, effects, desc, multiTargetFilters, 2, 2)` — tap to attach Equipment to creature, instant speed, uses `AttachTargetEquipmentToTargetCreatureEffect` |
| Transform DFC | `b/BloodlineKeeper.java` | Front face with `setBackFaceCard(new LordOfLineage())` + `getBackFaceClassName()` override. Activated ability with `TransformSelfEffect` + subtype count restriction (`CardSubtype.VAMPIRE, 5`). Back face is a separate Card subclass (`LordOfLineage`) |
| Werewolf transform DFC | `d/DaybreakRanger.java` | Innistrad werewolf pattern: front face uses EACH_UPKEEP_TRIGGERED with `NoSpellsCastLastTurnConditionalEffect(TransformSelfEffect())`. Back face (`NightfallPredator`) uses EACH_UPKEEP_TRIGGERED with `TwoOrMoreSpellsCastLastTurnConditionalEffect(TransformSelfEffect())`. Both faces have their own activated abilities |
| Werewolf lord DFC | `i/InstigatorGang.java` | Werewolf transform + STATIC `StaticBoostEffect(power, 0, OWN_CREATURES, PermanentIsAttackingPredicate())` on both faces (front +1/+0, back +3/+0). Back face (`WildbloodPack`) also has Trample (auto-loaded from Scryfall) |
| Upkeep reveal transform DFC | `d/DelverOfSecrets.java` | Non-werewolf upkeep transform: front face uses UPKEEP_TRIGGERED with `LookAtTopCardMayRevealTypeTransformEffect(Set.of(INSTANT, SORCERY))`. Looks at top card, if matching type offers may reveal + transform. Back face (`InsectileAberration`) is vanilla (no transform back trigger) |

## Sagas

| Pattern | Reference Card | Key Code |
|---------|-----------|-------|
| 3-chapter Saga with damage/life/token | `c/ChainersTorment.java` | SAGA_CHAPTER_I/II: DealDamageToEachOpponentEffect(2) + GainLifeEffect(2). SAGA_CHAPTER_III: CreateTokenFromHalfLifeTotalAndDealDamageEffect. Lore counters auto-managed: ETB adds 1st in StackResolutionService, precombat main adds subsequent in StepTriggerService. Sacrifice SBA in StateBasedActionService when lore >= final chapter and no chapter ability on stack |
| 3-chapter Saga with mass destruction + graveyard return | `f/FallOfTheThran.java` | SAGA_CHAPTER_I: DestroyAllPermanentsEffect(PermanentIsLandPredicate). SAGA_CHAPTER_II/III: EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(2, CardTypePredicate(LAND)). Each player returns up to 2 lands; auto-returns when ≤ maxCount, queues graveyard choices when > maxCount |
