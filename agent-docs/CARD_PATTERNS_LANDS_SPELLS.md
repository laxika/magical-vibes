# Card Patterns: Lands & Spells

All paths relative to `cards/`.

## Lands

| Pattern | Reference | Notes |
|---------|-----------|-------|
| Basic land | `f/Forest.java` | `addEffect(ON_TAP, AwardManaEffect(color))` |
| Pain land | `s/SulfurousSprings.java` | 3 activated abilities: colorless + 2x colored with DealDamageToController |
| Creature land (manland) | `t/TreetopVillage.java` | STATIC `EntersTappedEffect` + ON_TAP mana + AnimateLandEffect ability |
| Creature land (artifact) | `i/InkmothNexus.java` | manland that becomes artifact creature (uses 6-arg AnimateLandEffect with grantedCardTypes) |
| Creature land + sub-ability | `s/SpawningPool.java` | manland + regenerate with `ONLY_WHILE_CREATURE` restriction |
| X-cost land animation + counters | `w/WakerOfTheWilds.java` | {X}{G}{G} activated ability: PutCounterOnTargetPermanentEffect(PLUS_ONE_PLUS_ONE, new XValue()) + AnimateTargetPermanentEffect(0, 0, ELEMENTAL, HASTE) with ControlledPermanentPredicateTargetFilter(PermanentIsLandPredicate) |
| Check land | `d/DragonskullSummit.java` | STATIC `ConditionalReplacementEffect(new ControlsPermanentCountAtMost(0, PermanentHasAnySubtypePredicate), new EntersTappedEffect())` + 2 mana abilities — enters tapped unless you control a matching permanent |
| Fast land | `b/BlackcleaveCliffs.java` | STATIC `ConditionalReplacementEffect(new ControlsPermanentCount(3, new PermanentIsLandPredicate()), new EntersTappedEffect())` + 2 mana abilities — enters tapped if you control 3+ other lands |
| Slow land | `d/DeathcapGlade.java` | STATIC `ConditionalReplacementEffect(new ControlsPermanentCountAtMost(1, new PermanentIsLandPredicate()), new EntersTappedEffect())` + 2 mana abilities — enters tapped if you control 1 or fewer other lands |
| Utility land | `q/Quicksand.java` | mana ability + sacrifice-to-debuff ability |
| Utility land (exile-return) | `m/MystifyingMaze.java` | colorless mana ability + {4},{T}: exile target attacking creature, return tapped at end step (ExileTargetPermanentAndReturnAtEndStepEffect(true) + PermanentIsAttackingPredicate) |
| Utility land (destroy + each player search) | `f/FieldOfRuin.java` | colorless mana ability + {2},{T},Sacrifice: DestroyTargetAndEachPlayerSearchesBasicLandToBattlefieldEffect + opponent nonbasic land filter (PermanentAllOfPredicate: land + not basic + not controlled by source controller) |

## Spells

| Pattern | Reference | Notes |
|---------|-----------|-------|
| Targeted burn | `s/Shock.java` | SPELL DealDamageToAnyTargetEffect (targeting auto-derived) |
| Burn creature + controller | `c/ChandrasOutrage.java` | DealDamageToTargetCreatureEffect + DealDamageToTargetCreatureControllerEffect |
| Uncounterable + unpreventable burn | `c/Combust.java` | STATIC CantBeCounteredEffect + DealDamageToTargetCreatureEffect(5, true) + PermanentColorInPredicate target filter |
| X burn | `b/Blaze.java` | DealDamageToAnyTargetEffect(new XValue()) |
| Burn + life drain | `e/EssenceDrain.java` | DealDamageToAnyTargetAndGainLifeEffect |
| Burn + drain by land count | `c/Corrupt.java` | DealDamageToAnyTargetEqualToControlledSubtypeCountAndGainLifeEffect — damage and life gain equal to controlled Swamps |
| X drain all opponents | `e/Exsanguinate.java` | SPELL LoseLifeEffect(new XValue(), EACH_OPPONENT, true) — no target, X life loss from each opponent + controller gains total lost |
| Multi-target damage | `c/ConeOfFlame.java` | DealOrderedDamageToAnyTargetsEffect |
| Mixed target groups (mandatory + optional) | `s/SeismicShift.java` | Two separate `target()` calls with different filters and counts: `target(LandFilter)` (1,1) + `target(CreatureFilter, 0, 2)`. Destroy land + up to 2 creatures can't block. Also see `i/IntoTheMawOfHell.java` for two mandatory groups |
| Mixed graveyard + permanent targeting (up to one each) | `y/YawgmothsVileOffering.java` | Graveyard target via `addEffect(SPELL, ReturnCardFromGraveyardEffect.builder().targetGraveyard(true)...)` + permanent target via `target(filter, 0, 1).addEffect(SPELL, DestroyTargetPermanentEffect)` + `ExileSpellEffect`. Uses `targetId` for graveyard (Zone.GRAVEYARD) + `targetIds` for permanent. Legendary sorcery restriction handled automatically by Scryfall LEGENDARY supertype |
| Any number of target players life gain | `h/HuntersFeast.java` | EachTargetPlayerGainsLifeEffect(6) + setMinTargets(0) + setMaxTargets(99) — any number of target players each gain N life |
| X burn divided evenly | `f/Fireball.java` | DealXDamageDividedEvenlyAmongTargetsEffect + setMinTargets(1) + setMaxTargets(99) + setAdditionalCostPerExtraTarget(1) — X divided evenly (rounded down) among any number of targets, costs {1} extra per target beyond first |
| Divided damage to filtered creatures | `i/IgniteDisorder.java` | DealDividedDamageAmongTargetCreaturesEffect(3) + setMinTargets(1) + setMaxTargets(3) + PermanentPredicateTargetFilter(PermanentAllOfPredicate(IsCreature + ColorIn)) — N damage divided as you choose among up to M target creatures matching a color/type filter |
| Damage all creatures | `p/Pyroclasm.java` | MassDamageEffect |
| Modal spell (choose one) | `s/Slagstorm.java` | ChooseOneEffect wrapping multiple CardEffects (e.g. MassDamageEffect + DealDamageToEachPlayerEffect). Mode chosen at cast time via `xValue` parameter (0-based index). Test with `castSorcery(player, idx, modeIndex)` |
| Modal spell (per-mode target filter) | `c/CrushingCanopy.java`, `f/FieryIntervention.java` | Each `ChooseOneOption` has a 3rd arg `PermanentPredicateTargetFilter` overriding the cast-time target filter for that mode. No-target modes omit it. Modal cast derives targeting (permanent/spell/graveyard) from the chosen mode's unwrapped effect |
| Modal option with multiple effects | `w/WitherbloomCharm.java`, `l/LoreholdCharm.java`, `g/GloriousDecay.java` | A `ChooseOneOption` can take `List<CardEffect>` (e.g. "each opponent loses 3 / you gain 3", "creatures +1/+1 and trample", "exile from graveyard, draw a card"). All the mode's effects are spliced into resolution in order. Modal graveyard targeting (reanimate/exile from graveyard) works via the chosen mode's effects (Witherbloom/Lorehold/Glorious Decay) |
| Modal spell (choose one or both) | `r/RememberTheFallen.java` | ChooseOneEffect with 3 options: mode 0 (creature), mode 1 (artifact), mode 2 (both). Each wraps ReturnTargetCardsFromGraveyardToHandEffect with appropriate filter. "Both" mode uses CardAnyOfPredicate with maxTargets=2. SpellCastingService unwraps ChooseOneEffect before graveyard targeting detection |
| Modal "choose one or both" with two distinct spell targets | `c/ChoreographedSparks.java` | Mode 2 ("both") targets two *different* spells with two filters (one I/S + one creature you control). Use `new ChooseOneOption(label, List.of(effectA, effectB), List.of(filterA, filterB))` — the multi-`TargetFilter` overload. `unwrapChooseOneEffect` declares one `target()` slot per filter and maps each mode effect positionally (targetIds[0]→effectA, [1]→effectB); resolution uses the per-effect target-index override. Test the "both" mode with `harness.castModalInstant(player, idx, modeIndex, List.of(idA, idB))`. Instant-speed I/S part must be an actual instant so it can share the stack with a (sorcery-speed) creature spell |
| Damage filtered creatures + players | `h/Hurricane.java` | MassDamageEffect (X, filter, damagesPlayers) |
| Pump target | `g/GiantGrowth.java` | BoostTargetCreatureEffect |
| X pump target | `u/UntamedMight.java` | BoostTargetCreatureEffect(new XValue(), new XValue()) |
| Pump all + keyword | `o/Overrun.java` | BoostAllOwnCreaturesEffect + GrantKeywordEffect |
| Pump all + conditional keyword | `b/BreakOfDay.java` | BoostAllOwnCreaturesEffect + ConditionalEffect(new ControllerLifeAtMost(threshold), wrapped) wrapping GrantKeywordEffect — pump always, keyword only if condition met (fateful hour, morbid, raid, etc.) |
| Create N tokens + fateful hour "create M instead" | `g/GatherTheTownsfolk.java` | CreateTokenEffect(2, "Human", …) always + ConditionalEffect(new ControllerLifeAtMost(5), CreateTokenEffect(3, "Human", …)) — base count always, plus the *difference* (M−N) when fateful hour active. End count matches "create M instead" (2+3=5); identical result under token doublers/ETB triggers |
| Grant triggered bounce | `a/ArmWithAether.java` | GrantDamageToOpponentCreatureBounceUntilEndOfTurnEffect — grants "damage to opponent, may bounce creature" to all controlled creatures until end of turn |
| Protection choice (color or artifacts) | `a/ApostlesBlessing.java` | GrantProtectionChoiceUntilEndOfTurnEffect(true) + ControlledPermanentPredicateTargetFilter(AnyOf(artifact, creature)). Phyrexian mana. On resolution, player chooses color or artifacts → protection until end of turn |
| Protection choice + fateful hour "instead" | `f/FaithsShield.java` | target(ControlledPermanentPredicateTargetFilter(PermanentTrue)) with two **mutually exclusive** conditionals: ConditionalEffect(new ControllerLifeAtLeast(6), GrantProtectionChoiceUntilEndOfTurnEffect) [≥6 life → target only] and ConditionalEffect(new ControllerLifeAtMost(5), GrantProtectionChoiceToControllerAndPermanentsUntilEndOfTurnEffect) [≤5 life → you + all your permanents]. Only one fires → single color prompt. Spell always requires the target (fizzles if it becomes illegal). Pattern for any "fateful hour — instead" spell |
| Pump attacking + keyword | `r/RallyTheForces.java` | BoostAllCreaturesEffect(filter) + GrantKeywordEffect(ALL_CREATURES, filter) with PermanentIsAttackingPredicate |
| Targeted destroy | `t/Terror.java` | DestroyTargetPermanentEffect + target filter |
| Targeted destroy (power filter) | `s/SmiteTheMonstrous.java` | DestroyTargetPermanentEffect + PermanentPowerAtLeastPredicate(4) creature filter |
| Multi-effect removal | `c/Condemn.java` | PutTargetOnBottomOfLibrary + GainLifeEqualToTargetToughness |
| Put on top of library | `b/BanishmentDecree.java` | PutTargetOnTopOfLibraryEffect + PermanentAnyOfPredicate filter (artifact/creature/enchantment) |
| Metalcraft sacrifice instant | `d/DispenseJustice.java` | SacrificeAttackingCreaturesEffect(1, 2) + PlayerPredicateTargetFilter(ANY) — metalcraft checked at resolution |
| Destroy + cantrip | `s/Smash.java` | DestroyTargetPermanentEffect + DrawCardEffect |
| Destroy + life gain by mana value | `d/DivineOffering.java` | DestroyTargetPermanentThenEffect(MANA_VALUE, GainLifeEffect(EventValue()), CONTROLLER) + artifact filter |
| Destroy + controller life loss | `g/GlissasScorn.java` | DestroyTargetPermanentThenEffect(LoseLifeEffect(1), TARGET_CONTROLLER) + artifact filter |
| Board wipe | `w/WrathOfGod.java` | DestroyAllPermanentsEffect |
| Board wipe + opponent library search to graveyard | `l/LifesFinale.java` | DestroyAllPermanentsEffect + SearchTargetLibraryForCardsToGraveyardEffect(3, CREATURE) + PlayerPredicateTargetFilter(OPPONENT) |
| Destroy land + mass creature/planeswalker damage | `s/StarOfExtinction.java` | DestroyTargetPermanentEffect + MassDamageEffect(20, false, false, true, null) — targets a land, deals 20 damage to each creature and each planeswalker (damagesPlaneswalkers=true) |
| Opponent land edict | `y/YawningFissure.java` | SacrificePermanentsEffect(1, PermanentIsLandPredicate, EACH_OPPONENT) — each opponent sacrifices a land, controller unaffected |
| Counter (any) | `c/Cancel.java` | CounterSpellEffect (spell targeting auto-derived) |
| Counter (filtered by type) | `r/RemoveSoul.java` | StackEntryPredicateTargetFilter + StackEntryTypeInPredicate |
| Counter (filtered by mana value) | `m/MentalMisstep.java` | StackEntryPredicateTargetFilter + StackEntryManaValuePredicate. Phyrexian mana cost |
| Counter + bonus | `d/Discombobulate.java` | Counter + ReorderTopCardsOfLibraryEffect |
| Counter + bounce | `l/LostInTheMist.java` | CounterSpellEffect + ReturnTargetPermanentToHandEffect — targets both a spell and a permanent. Uses `targetId` (spell, Zone.STACK) + `targetIds` (permanent). Multi-zone fizzle: only fizzles if ALL targets become illegal |
| Counter (filtered) + draw | `b/BoneToAsh.java` | CounterSpellEffect + DrawCardEffect + creature-spell filter via target() chain |
| Counter (filtered) + life loss | `p/PsychicBarrier.java` | TargetSpellControllerLosesLifeEffect(1) + CounterSpellEffect + creature-spell filter. Life loss placed before counter so target is still on stack |
| Counter-unless-pay + discard | `f/FrightfulDelusion.java` | TargetSpellControllerDiscardsEffect(1) + CounterUnlessPaysEffect(1). Discard placed before counter so target is still on stack |
| Counter + metalcraft cost reduction | `s/StoicRebuttal.java` | CounterSpellEffect + STATIC ConditionalEffect(Metalcraft(), ReduceOwnCastCostEffect(Fixed(1))) — costs {1} less with 3+ artifacts |
| Counter (conditional, poisoned) | `c/CorruptedResolve.java` | CounterSpellIfControllerPoisonedEffect — counters only if target spell's controller is poisoned |
| Counter spell/ability (sac creature) | `s/SirenStormtamer.java` | Activated: {U}+SacrificeSelfCost+CounterSpellEffect with StackEntryAllOfPredicate(StackEntryHasTargetPredicate, StackEntryTargetsYouOrCreatureYouControlPredicate) — counters spells OR abilities that target you or a creature you control. HasTargetPredicate enables ability targeting |
| Counter + may cast from hand | `c/Counterlash.java` | CounterlashEffect — counters target spell, then queues per-eligible-card MayCastFromHandWithoutPayingManaCostEffect may abilities for cards sharing a type |
| Bounce target | `u/Unsummon.java` | ReturnTargetPermanentToHandEffect |
| Bounce target + life loss | `v/VaporSnag.java` | ReturnTargetPermanentToHandEffect(1) — bounce creature, its controller loses life |
| Bounce creature + conditional draw | `t/TemporalMachinations.java` | SPELL ReturnTargetPermanentToHandEffect() + SPELL ConditionalEffect(new ControlsPermanent(PermanentIsArtifactPredicate()), DrawCardEffect(1)) — bounce target creature, if you control an artifact draw a card. Use ConditionalEffect(new ControlsPermanent(filter), wrapped) for "if you control a [type]" conditions |
| Bounce mass | `e/Evacuation.java` | ReturnCreaturesToOwnersHandEffect |
| Bounce mass (target player, filtered) | `r/RiversRebuke.java` | ReturnPermanentsTargetPlayerControlsToHandEffect(PermanentNotPredicate(PermanentIsLandPredicate())) — return all nonland permanents target player controls to their owners' hands |
| Mass exile + return at step (target player, filtered) | `s/SuddenDisappearance.java` | ExilePermanentsTargetPlayerControlsAndReturnAtStepEffect(PermanentNotPredicate(PermanentIsLandPredicate()), TurnStep.END_STEP) — exile all nonland permanents target player controls, return at next end step |
| Pure draw | `c/CounselOfTheSoratami.java` | DrawCardEffect |
| Draw + discard | `s/Sift.java` | DrawCardEffect + DiscardEffect(1, CONTROLLER) |
| Each player draw + random discard | `b/BurningInquiry.java` | EachPlayerDrawsCardEffect + DiscardEffect(3, EACH_PLAYER, true) |
| Library selection (hand/top/bottom) | `t/TellingTime.java` | LookAtTopCardsHandTopBottomEffect |
| Library selection (N to hand, rest to graveyard) | `f/ForbiddenAlchemy.java` | LookAtTopCardsChooseNToHandRestToGraveyardEffect(count, toHandCount) |
| Library selection + self-damage | `d/DarkBargain.java` | LookAtTopCardsChooseNToHandRestToGraveyardEffect(3, 2) + DealDamageToControllerEffect(2) |
| Library reveal (type to hand, rest to graveyard) | `m/Mulch.java` | RevealTopCardsTypeToHandRestToGraveyardEffect(count, cardTypes) — deterministic, no player choice |
| Library match-permanent-to-battlefield | `m/MitoticManipulation.java` | LookAtTopCardsPutMatchingPermanentNameOnBattlefieldEffect |
| Aura upkeep — library creature sharing type to battlefield | `c/CallToTheKindred.java` | MayEffect wrapping LookAtTopCardsCreatureSharingTypeWithEnchantedToBattlefieldEffect(5) in UPKEEP_TRIGGERED |
| Targeted discard | `d/Distress.java` | ChooseCardFromTargetHandToDiscardEffect |
| Exile by name (multi-zone) | `m/Memoricide.java` | ChooseCardNameAndExileFromZonesEffect(excludedTypes) — choose nonland name, exile from hand+graveyard+library, shuffle |
| Tutor to hand | `d/DiabolicTutor.java` | SearchLibraryForCardsToHandEffect() — unified search-to-hand; no-arg = any single card, `(filter)` = restricted, `(filter,count,castFromGraveyardCount)` for multi/increasing |
| Tutor + exile + opponent choice | `d/DistantMemories.java` | DistantMemoriesEffect — search, exile, opponent may let you have it or draw 3 |
| Tutor to battlefield | `r/RampantGrowth.java` | SearchLibraryForCardTypesToBattlefieldEffect |
| Cultivate (2 basic lands split) | `c/Cultivate.java` | SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect — one to BF tapped, one to hand |
| Graveyard return (to hand) | `r/Recollect.java` | ReturnCardFromGraveyardEffect.builder().destination(HAND).targetGraveyard(true).build() — any card, targets graveyard |
| Graveyard return (multi-target to hand) | `m/MorbidPlunder.java` | ReturnTargetCardsFromGraveyardToHandEffect(CardTypePredicate(CREATURE), 2) — up to N target cards to hand |
| Graveyard return (one per subtype to hand) | `g/GrimCaptainsCall.java` | ReturnOneOfEachSubtypeFromGraveyardToHandEffect(List.of(PIRATE, VAMPIRE, DINOSAUR, MERFOLK)) — one of each subtype, chosen at resolution |
| Graveyard return (to battlefield) | `b/BeaconOfUnrest.java` | ReturnCardFromGraveyardEffect.builder().destination(BATTLEFIELD).filter(CardAnyOfPredicate).source(ALL_GRAVEYARDS).build() |
| Graveyard reanimate + type/color grant | `r/RiseFromTheGrave.java` | ReturnCardFromGraveyardEffect.builder().destination(BATTLEFIELD).source(ALL_GRAVEYARDS).grantColor(BLACK).grantSubtype(ZOMBIE).build() — permanently adds color and subtype "in addition to" |
| Graveyard to top of owner's library | `n/NoxiousRevival.java` | ReturnCardFromGraveyardEffect.builder().destination(TOP_OF_OWNERS_LIBRARY).source(ALL_GRAVEYARDS).targetGraveyard(true).build() — any card from any graveyard on top of owner's library. Phyrexian mana |
| Graveyard to top of library + draw | `f/FranticSalvage.java` | PutTargetCardsFromGraveyardOnTopOfLibraryEffect(CardTypePredicate(ARTIFACT)) + DrawCardEffect — any number of target artifact cards, multi-graveyard targeting at cast time |
| Graveyard shuffle into library (target player) | `m/MemorysJourney.java` | ShuffleTargetCardsFromGraveyardIntoLibraryEffect(null, 3) + FlashbackCast("{G}") — target player shuffles up to 3 cards from their graveyard into their library, targets any player's graveyard |
| Graveyard shuffle + draw + self-mill trigger | `g/GaeasBlessing.java` | ShuffleTargetCardsFromGraveyardIntoLibraryEffect(null, 3) + DrawCardEffect(1) + ON_SELF_MILLED ShuffleGraveyardIntoLibraryEffect(false) — shuffle up to 3 cards from target player's graveyard into library, draw, and when milled shuffle owner's graveyard into library |
| Life gain per graveyard card + self-shuffle | `a/ArchangelsLight.java` | GainLifeEffect(Scaled(CardsInGraveyard(null, CONTROLLER), 2)) + ShuffleGraveyardIntoLibraryEffect(false) — gain 2 life per card in your graveyard, then shuffle your graveyard into your library (non-targeting) |
| Exile return to hand (filtered, owned) | `r/RunicRepetition.java` | ReturnTargetCardFromExileToHandEffect(CardHasFlashbackPredicate(), true) — return target exiled card with flashback you own to hand. `ownedOnly=true` restricts to controller's exile zone. Targets exile (`canTargetExile()=true`) |
| Prevent combat damage | `h/HolyDay.java` | PreventAllCombatDamageEffect |
| Prevent combat damage (selective) | `m/Moonmist.java` | TransformAllEffect(PermanentHasSubtypePredicate(HUMAN)) + PreventCombatDamageExceptBySubtypesEffect(PermanentHasAnySubtypePredicate(WEREWOLF, WOLF)) — transform all Humans, prevent combat damage by non-Werewolves/Wolves |
| Steal creature (temp) | `t/Threaten.java` | GainControlOfTargetCreatureUntilEndOfTurn + haste + untap |
| Steal artifact (temp) | `m/MetallicMastery.java` | GainControlOfTargetPermanentUntilEndOfTurn + untap + haste + PermanentIsArtifactPredicate filter |
| Steal creature (permanent, tap ability, power check) | `b/BeguilerOfWills.java` | Tap ability + GainControlOfTargetPermanentEffect + PermanentAllOfPredicate(PermanentIsCreaturePredicate, PermanentPowerAtMostControlledCreatureCountPredicate) target filter — gain control of creature with power <= your creature count |
| Extra turn | `t/TimeStretch.java` | ExtraTurnEffect |
| Extra combat | `r/RelentlessAssault.java` | AdditionalCombatMainPhaseEffect |
| Mill half library (spell) | `t/Traumatize.java` | MillHalfLibraryEffect(false) |
| Mill half library (ON_ATTACK) | `f/FleetSwallower.java` | ON_ATTACK MillHalfLibraryEffect(true) — creature attacks, target player mills half library rounded up |
| Shuffle-back spell | `b/BeaconOfDestruction.java` | Effect + ShuffleIntoLibraryEffect |
| X draw (controller) | `m/MindSpring.java` | DrawCardEffect(new XValue()) — non-targeting X draw for controller |
| X burn + exile-instead-of-die + shuffle | `r/RedSunsZenith.java` | DealDamageToAnyTargetEffect(new XValue(), false, true) + ShuffleIntoLibraryEffect |
| X tokens + shuffle | `w/WhiteSunsZenith.java` | CreateTokenEffect(new XValue(), ...) + ShuffleIntoLibraryEffect |
| Fight (two-target) | `p/PreyUpon.java` | FirstTargetFightsSecondTargetEffect, multi-target: creature you control + creature you don't control |
| Fight (any two creatures) | `b/BloodFeud.java` | FirstTargetFightsSecondTargetEffect, both targets are any creature (no controller restriction). Cross-group distinct is the default — no extra flags needed when oracle text says "another target" |
| Counter + target spell cost reduction | `b/BrushOff.java` | STATIC ReduceOwnCastCostIfTargetingStackEntryEffect(StackEntryTypeInPredicate(INSTANT_SPELL, SORCERY_SPELL), 2) + SPELL CounterSpellEffect — costs {1}{U} less when targeting an instant or sorcery spell, counters any spell |
| Fight + counter + target cost reduction | `s/SavageStomp.java` | STATIC ReduceOwnCastCostIfTargetingControlledPermanentEffect(PermanentHasSubtypePredicate(DINOSAUR), 2) + SPELL PutPlusOnePlusOneCounterOnFirstTargetEffect(1) + FirstTargetFightsSecondTargetEffect — costs less when targeting a matching controlled permanent, puts +1/+1 counter on first target then fights |
| Bite (pump + bite) | `a/AssertPerfection.java` | BoostFirstTargetCreatureEffect + FirstTargetDealsPowerDamageToSecondTargetEffect, multi-target with per-position filters |
| Pump + debuff (two targets, shared OK) | `l/LeechingBite.java` | BoostFirstTargetCreatureEffect + BoostSecondTargetCreatureEffect, multi-target with creature filters. Uses `setAllowSharedTargets(true)` because oracle text has no "another" and both targets accept any creature |
| Damage creature + destroy equipment | `t/TurnToSlag.java` | DestroyEquipmentAttachedToTargetCreatureEffect + DealDamageToTargetCreatureEffect — effect order doesn't matter; lethal damage destruction is deferred until all effects on the stack entry resolve |
| Sacrifice artifact spell cost + tokens | `k/KuldothaRebirth.java` | SacrificeArtifactCost + CreateTokenEffect — sacrifice artifact as additional spell cost |
| Sacrifice permanent spell cost + burn | `a/Artillerize.java` | SacrificePermanentCost(PermanentAnyOfPredicate) + DealDamageToAnyTargetEffect — sacrifice artifact or creature as additional spell cost |
| Sacrifice creature spell cost + power-based mass debuff | `i/IchorExplosion.java` | SacrificeCreatureCost(false, true) + BoostAllCreaturesEffect(new Scaled(new XValue(), -1), new Scaled(new XValue(), -1)) — sacrifice creature, all creatures get -X/-X where X = sacrificed creature's power |
| Exile graveyard creature spell cost + power-based damage | `c/CorpseLunge.java` | ExileCardFromGraveyardCost(CREATURE, false, false, true) + DealDamageToTargetCreatureEffect(new XValue()) — exile creature from graveyard as additional cost (snapshots the exiled card's power into xValue), deal that much damage to target creature |
| Graveyard-count damage | `s/ScrapyardSalvo.java` | DealDamageToTargetPlayerEffect(new CardsInGraveyard(new CardTypePredicate(ARTIFACT), CountScope.CONTROLLER)) — damage to target player equal to artifact cards in your graveyard |
| Mass exile + reveal creatures to battlefield | `m/MassPolymorph.java` | ExileAllCreaturesYouControlThenRevealCreaturesToBattlefieldEffect — exile all your creatures, reveal from library until that many creature cards found, put onto battlefield, shuffle rest back |
| Static self-boost per controlled subtype | `e/EarthServant.java` | STATIC BoostSelfEffect(Fixed(0), PermanentCount(PermanentHasSubtypePredicate(MOUNTAIN), CONTROLLER)) — +0/+1 for each Mountain you control |
| Static conditional boost (another multi-subtype) | `k/KumenasSpeaker.java` | STATIC ConditionalEffect(new ControlsAnotherPermanent(PermanentHasAnySubtypePredicate(Set.of(MERFOLK, ISLAND))), StaticBoostEffect(1, 1, SELF)) — +1/+1 as long as you control another Merfolk or Island |
| Flashback spell | `a/AncientGrudge.java` | `addCastingOption(new FlashbackCast("{G}"))` + normal effects/targeting. Cast from graveyard for flashback cost, exiled after resolving or fizzling. Flashback is a spell cast (counterable, triggers "whenever you cast"), not an activated ability. |
| Exile target + all same name | `s/SeverTheBloodline.java` | ExileTargetCreatureAndAllWithSameNameEffect + FlashbackCast("{5}{B}{B}") — exile target creature and all other creatures with the same name |
| Grant flashback to graveyard | `p/PastInFlames.java` | `GrantFlashbackToGraveyardCardsEffect(Set.of(CardType.INSTANT, CardType.SORCERY))` + own `FlashbackCast("{4}{R}")`. Grants flashback (cost = mana cost) to matching cards in controller's graveyard until end of turn. Tracked in `GameData.cardsGrantedFlashbackUntilEndOfTurn`. |
| ETB grant flashback to target graveyard card | `s/SnapcasterMage.java` | `GrantFlashbackToTargetGraveyardCardEffect(Set.of(CardType.INSTANT, CardType.SORCERY))` on ON_ENTER_BATTLEFIELD. Flash creature. Targets a single instant/sorcery in controller's graveyard, grants flashback (cost = mana cost) until end of turn. Uses multi-graveyard targeting at ETB trigger time. |
