# Card Patterns: Vanilla, Keyword & ETB Creatures

All paths relative to `cards/`.

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
| Alternate casting cost (mana + tap artifact) | `z/ZahidDjinnOfTheLamp.java` | `addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{3}{U}"), new TapUntappedPermanentsCost(1, new PermanentIsArtifactPredicate()))))` — "You may pay {3}{U} and tap an untapped artifact you control rather than pay this spell's mana cost." Frontend sends tapped artifact ID in `alternateCostSacrificePermanentIds`. Keywords (flying) auto-loaded from Scryfall. |
| ETB + discard-to-battlefield replacement | `o/ObstinateBaloth.java` | ON_SELF_DISCARDED_BY_OPPONENT `EnterBattlefieldOnDiscardEffect` + ON_ENTER_BATTLEFIELD GainLifeEffect. Replacement effect: if opponent causes you to discard this card, put it onto the battlefield instead of graveyard. ETB triggers still fire. |

## ETB creatures

| Pattern | Reference | Notes |
|---------|-----------|-------|
| ETB pump target creature | `b/BriarpackAlpha.java` | `target(CreaturePredicate).addEffect(ON_ENTER_BATTLEFIELD, BoostTargetCreatureEffect(P, T))` — any creature. See also `v/VulshokHeartstoker.java` (+2/+0). Flash is auto-loaded from Scryfall, no constructor code needed |
| ETB pump target creature (own only, +keyword) | `i/ImperialAerosaur.java` | PermanentAllOfPredicate(IsCreature, ControlledBySourceController, NotSource) + BoostTargetCreatureEffect + GrantKeywordEffect(FLYING, TARGET) |
| ETB gain life | `a/AngelOfMercy.java` | ON_ENTER_BATTLEFIELD GainLifeEffect |
| ETB draw | `k/KavuClimber.java` | ON_ENTER_BATTLEFIELD DrawCardEffect |
| ETB self-mill | `a/ArmoredSkaab.java` | ON_ENTER_BATTLEFIELD MillControllerEffect(4) — controller mills N cards, no target |
| ETB draw + downside | `p/PhyrexianRager.java` | Draw + LoseLifeEffect |
| ETB destroy (targeted) | `n/Nekrataal.java` | ON_ENTER_BATTLEFIELD DestroyTargetPermanentEffect (targeting auto-derived) |
| ETB destroy (conditional, dealt damage) | `f/FathomFleetCutthroat.java` | ON_ENTER_BATTLEFIELD DestroyTargetPermanentEffect + PermanentAllOfPredicate(IsCreature, NotControlledBySource, DealtDamageThisTurn) |
| ETB may destroy (filtered) | `a/AcidWebSpider.java` | MayEffect(DestroyTargetPermanentEffect) + PermanentPredicateTargetFilter |
| ETB destroy all (predicate) + static hexproof | `w/WitchbaneOrb.java` | DestroyAllPermanentsEffect(AllOf(HasSubtype(CURSE), AttachedToSourceController)) + GrantControllerHexproofEffect STATIC |
| ETB set target player life total | `t/TorgaarFamineIncarnate.java` | STATIC SacrificeCreaturesForCostReductionEffect(2) + ON_ENTER_BATTLEFIELD SetTargetPlayerLifeToHalfStartingEffect() with `target(PlayerPredicateTargetFilter(ANY), 0, 1)` for "up to one target player". Sacrifice creatures as additional cost to reduce mana by 2 per creature |
| ETB may exile until leaves (O-ring) | `l/LeoninRelicWarder.java` | MayEffect(ExileTargetPermanentUntilSourceLeavesEffect) + PermanentPredicateTargetFilter(AnyOf(artifact, enchantment)). Exiled card returns when source leaves battlefield |
| ETB may exile + return at end step (flicker) | `s/SentinelOfThePearlTrident.java` | MayEffect(ExileTargetPermanentAndReturnAtEndStepEffect) + PermanentPredicateTargetFilter(AllOf(ControlledBySourceController, PermanentIsHistoricPredicate)). Flash creature that flickers own historic permanent |
| Instant flicker (immediate return) + subtype bonus | `s/SirensRuse.java` | ExileTargetPermanentAndReturnImmediatelyEffect(CardSubtype.PIRATE, DrawCardEffect(1)) + PermanentPredicateTargetFilter(AllOf(ControlledBySourceController, IsCreature)). Instant that exiles own creature and immediately returns it; draws a card if it was a Pirate |
| ETB may rummage (discard then draw) | `k/KeldonRaider.java` | MayEffect(DiscardAndDrawCardEffect()) — may discard a card, if you do draw a card |
| ETB draw + random discard + conditional counters | `r/RowdyCrew.java` | DrawAndRandomDiscardWithSharedTypeCountersEffect(3, 2, 2) — draw N, discard M at random, +1/+1 counters if discards share a card type |
| ETB discard (targeted) | `r/RavenousRats.java` | TargetPlayerDiscardsEffect |
| ETB discard (raid conditional) | `d/DeadeyeTormentor.java` | RaidConditionalEffect(TargetPlayerDiscardsEffect(1)) + PlayerPredicateTargetFilter(OPPONENT). Raid = intervening-if checked at trigger and resolution time |
| ETB discard (each opponent) | `l/LilianasSpecter.java` | EachOpponentDiscardsEffect — no targeting, all opponents discard |
| ETB search | `c/CivicWayfinder.java` | MayEffect(SearchLibraryForBasicLandToHandEffect) |
| ETB opponent search (downside) | `o/OldGrowthDryads.java` | EachOpponentMaySearchLibraryForBasicLandToBattlefieldTappedEffect — each opponent may search for a basic land (tapped). No targeting. APNAP order |
| ETB search (type + min MV) | `t/TreasureMage.java` | MayEffect(SearchLibraryForCardTypesToHandEffect(ARTIFACT, 6, MAX_VALUE)) — artifact with MV 6+ |
| ETB search (by name, multi-pick) | `s/SquadronHawk.java` | MayEffect(SearchLibraryForCardsByNameToHandEffect("Squadron Hawk", 3)) — search for up to 3 copies by name to hand |
| ETB may return from GY | `g/Gravedigger.java` | MayEffect(ReturnCardFromGraveyardEffect.builder().destination(HAND).filter(CardTypePredicate(CREATURE)).build()) |
| ETB may bounce own historic | `g/GuardiansOfKoilos.java` | MayEffect(ReturnTargetPermanentToHandEffect()) + PermanentPredicateTargetFilter(AllOf(AnyOf(artifact, legendary, Saga), controlled-by-source, not-source)) — "you may return another target historic permanent you control to its owner's hand" |
| ETB cast from opponent's GY | `c/ChancellorOfTheSpires.java` | CastTargetInstantOrSorceryFromGraveyardEffect(OPPONENT_GRAVEYARD, true) — targets instant/sorcery in opponent's graveyard, may cast without paying. Also has ON_OPENING_HAND_REVEAL MayEffect(EachOpponentMillsEffect(7)) |
| ETB explore | `b/BrazenBuccaneers.java` | ON_ENTER_BATTLEFIELD ExploreEffect() — reveal top card; land → hand, non-land → +1/+1 counter + may put to graveyard |
| Activated: exile from opponent GY + explore | `d/DeadeyeTracker.java` | Tap + {1}{B}: ExileTargetCardsFromOpponentGraveyardEffect(2) + ExploreEffect — exile two target cards from opponent's graveyard, then this creature explores. Uses `activateAbilityWithGraveyardTargets` for multi-target graveyard activated abilities |
| Explore trigger (target opponent creature) | `l/LurkingChupacabra.java` | ON_ALLY_CREATURE_EXPLORES BoostTargetCreatureEffect(-2, -2) — whenever a creature you control explores, target creature an opponent controls gets -2/-2. Uses `pendingExploreTriggerTargets` queue for target selection |
| ETB tokens + ability | `s/SiegeGangCommander.java` | CreateTokenEffect + activated sac ability |
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
| ETB +1/+1 counters per creature deaths this turn | `b/BloodcrazedPaladin.java` | ON_ENTER_BATTLEFIELD EnterWithPlusOnePlusOneCountersPerCreatureDeathsThisTurnEffect() — Flash creature, enters with +1/+1 counter for each creature that died this turn (all players). Flash keyword auto-loaded from Scryfall |
| ETB +1/+1 counters per subtype | `u/UnbreathingHorde.java` | ON_ENTER_BATTLEFIELD EnterWithPlusOnePlusOneCountersPerSubtypeEffect(ZOMBIE, true) + STATIC PreventDamageAndRemovePlusOnePlusOneCountersEffect(true) — enters with counters per Zombie on battlefield + graveyard, damage prevents and removes one counter |
| ETB each player poison | `i/IchorRats.java` | ON_ENTER_BATTLEFIELD GiveEachPlayerPoisonCountersEffect(1) — infect creature, each player gets a poison counter |
| ETB may-sacrifice-artifact-divided-damage | `k/KuldothaFlamefiend.java` | MayEffect(SacrificeArtifactThenDealDividedDamageEffect(4)) — may sacrifice artifact, deal N divided damage. Damage assignments via `pendingETBDamageAssignments`, artifact choice via PermanentChoiceContext.SacrificeArtifactForDividedDamage |
| ETB+attack divided damage + firebreathing | `i/InfernoTitan.java` | DealDividedDamageToAnyTargetsEffect(3, 3) on ON_ENTER_BATTLEFIELD + ON_ATTACK, plus {R} BoostSelfEffect(1,0) activated ability. Damage assignments via `pendingETBDamageAssignments` |
| ETB+attack may-search multi-land | `p/PrimevalTitan.java` | MayEffect(SearchLibraryForCardTypesToBattlefieldEffect(LAND, false, true, 2)) on ON_ENTER_BATTLEFIELD + ON_ATTACK. Multi-pick search via maxCount=2. MayEffect separated from mandatory effects in CombatAttackService for attack triggers |
| ETB modal (choose one, no target) | `b/BrutalizerExarch.java` | ChooseOneEffect on ON_ENTER_BATTLEFIELD with one non-targeting mode (library search) and one targeting mode. Card-level setTargetFilter for the targeting mode. Mode chosen at cast time via xValue |
| ETB modal (choose one, per-mode targeting) | `d/DeceiverExarch.java` | ChooseOneEffect on ON_ENTER_BATTLEFIELD with per-mode TargetFilter on each ChooseOneOption. Mode 0: UntapTargetPermanentEffect + PermanentControlledBySourceControllerPredicate. Mode 1: TapTargetPermanentEffect + PermanentNotPredicate(PermanentControlledBySourceControllerPredicate). Flash creature |
| ETB search-exile-imprint + death return to hand | `h/HoardingDragon.java` | MayEffect(SearchLibraryForCardTypeToExileAndImprintEffect(ARTIFACT)) + MayEffect(PutImprintedCardIntoOwnersHandEffect) — ETB searches library for artifact to exile/imprint, death returns exiled card to owner's hand |
| Kicker creature (ETB counters if kicked) | `a/AcademyDrake.java` | STATIC KickerEffect("{4}") + ON_ENTER_BATTLEFIELD EnterWithPlusOnePlusOneCountersIfKickedEffect(2) — kicker is optional additional mana cost, if paid the creature enters with +1/+1 counters |
| Kicker creature (ETB mass bounce if kicked) | `s/SlinnVodaTheRisingDeep.java` | STATIC KickerEffect("{1}{U}") + ON_ENTER_BATTLEFIELD KickedConditionalEffect(ReturnCreaturesToOwnersHandEffect(filters)) — if kicked, bounces all creatures except those with specified subtypes. Uses PermanentNotPredicate(PermanentHasAnySubtypePredicate(subtypes)) to exempt specific creature types from the bounce |
| Kicker instant (conditional spell effect) | `b/BlinkOfAnEye.java` | STATIC KickerEffect("{1}{U}") + SPELL ReturnTargetPermanentToHandEffect() + SPELL KickedConditionalEffect(DrawCardEffect()) — bounce nonland permanent, if kicked also draw a card. Use KickedConditionalEffect to wrap any spell effect that should only resolve when kicked |
| Kicker sorcery (replacement effect) | `f/FightWithFire.java` | STATIC KickerEffect("{5}{R}") + SPELL KickerReplacementEffect(DealDamageToTargetCreatureEffect(5), DealDividedDamageAmongAnyTargetsEffect(10)) — deals 5 damage to target creature; if kicked, deals 10 damage divided among any targets instead. Use KickerReplacementEffect when kicked mode *replaces* the base mode ("instead" in oracle text) |
| Kicker sorcery (sacrifice kicker + additional target) | `g/GoblinBarrage.java` | STATIC KickerEffect(PermanentAnyOfPredicate(...), "an artifact or Goblin") + SPELL DealDamageToTargetCreatureEffect(4) + SPELL KickedConditionalEffect(DealDamageToSecondaryTargetEffect(4)) — sacrifice-based kicker cost with additional player target when kicked. Primary target in targetId, kicked target in targetIds |


