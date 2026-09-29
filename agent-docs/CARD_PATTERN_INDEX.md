# CARD_PATTERN_INDEX
| land enters tapped, taps for blue, or pays {1}{U} plus seven graveyard cards for blue mana that copies the next spell or activated ability | `s/SunkenPalace.java` + `EntersTappedEffect` + `ExileNCardsFromGraveyardCost(7)` + `AwardManaEffect(ManaColor.BLUE)` + `RegisterNextSpellOrAbilityCopyEffect` (M3C 81) |
| land with colorless mana, sacrifice-a-creature any-color mana, and sorcery-speed X Desert sacrifice/exile-copy ability | `l/LazotepQuarry.java` + `ManaAbilities.tapFor` + `SacrificeCreatureCost`/`AwardAnyColorManaEffect` + `SacrificePermanentCost`/`ExileTargetCardFromGraveyardAndCreateTokenCopyEffect` filtered by `CardManaValueEqualsXPredicate` (M3C 79) |
| life gain turns into energy, with a tap-and-mana ability that pays X energy to draw X | `s/SphinxOfTheRevelation.java` + `ON_CONTROLLER_GAINS_LIFE EnergyCountersEffect(new EventValue())` + `ActivatedAbility(... PayXEnergyCost(), DrawCardEffect(new XValue())).withXValue()` (M3C 75) |
| energy-paid cost reduction plus optional paid attack trigger | `b/BlasterHulk.java` + `ReduceOwnCastCostEffect(new EnergyCountersPaidOrLostThisTurn())` + `ON_ATTACK EnergyCountersEffect(2)` + `target(0, 8)` with `ForcedCostOrElseEffect(PayEnergyCost(8), ..., true, DealDividedDamageEffect(...))` (M3C 55) |
| Vehicle attack trigger optionally pays energy, then targets an instant or sorcery in the controller's graveyard for jump-start | `f/FiligreeRacer.java` + `ForcedCostOrElseEffect(PayEnergyCost(2), ..., true, QueueReflexiveAbilityEffect(GrantJumpStartToTargetGraveyardCardEffect()))` (M3C 56) |
| tap ability gains energy, optionally pays one or more energy, and creates an X/X token sized by the payment while doubling energy gains | `a/AetherRefinery.java` + `STATIC DoubleEnergyCountersEffect` + `EnergyCountersEffect(1)` + `PayAnyAmountOfEnergyToCreateTokenEffect(CreateTokenEffect(... EventValue(), EventValue() ...))` |
| cast trigger chooses one creature controlled by each opponent, then creates one aggregate-P/T colorless Eldrazi token copy | `b/BenthicAnomaly.java` + `EachOpponentChoosesCreatureCreateTokenCopyWithTotalPowerToughnessEffect` |
| equipment attack trigger gains energy, then boosts itself or its equipped creature by current energy, with mana or energy reconfigure | `r/RazorfieldRipper.java` + `ON_ATTACK EnergyCountersEffect(1)` + `BoostSelfOrEnchantedCreatureUntilEndOfTurnEffect(ControllerEnergyCounters(), ControllerEnergyCounters())` + separate `{2}` and `PayEnergyCost(3)` reconfigure abilities |
| attack trigger chooses odd/even and temporarily prevents creatures with that mana-value parity from blocking, while conditional bloodthirst grants extra counters to other colorless creatures | `t/TwinsOfDiscord.java` + `SequenceEffect.of(ChooseManaValueParityAtResolutionEffect, CreaturesOfChosenManaValueParityCantBlockThisTurnEffect)` + printed bloodthirst entry replacement + conditional `ControlledPermanentsEnterWithAdditionalCountersEffect` |
| combat damage from a creature prevents and shuffles the damaged creature | `w/WeepingAngel.java` + `PreventCombatDamageBySelfToCreaturesAndShuffleEffect` |
| artifact creature death → may exile it, then highest-life opponent faces a villainous choice | `t/TheMasterGallifreysEnd.java` + `TheMasterGallifreysEndEffect` |
| activated ability offers a suspended hand spell for its suspend cost | `t/TheFaceOfBoe.java` + `MayCastSpellWithSuspendCostFromHandEffect` |
| turn-scoped shuffle replacement for creatures entering from exile or cast from exile | `d/DontBlink.java` + `ShuffleCreaturesEnteringFromExileEffect` |
| equipment attack counter plus combat-damage d12 comparison that doubles attached-creature +1/+1 counters | `s/SwordOfHours.java` + `RollD12AndResolveIfGreaterThanEventValueEffect` + `DoublePlusOneCountersOnEnchantedCreatureEffect` |
| temporary triggered ability on all opponent permanents, including later entrants | `h/HellishRebuke.java` + `GrantStaticEffectToOpponentPermanentsUntilEndOfTurnEffect` + `GrantTriggeredAbilityEffect` |
| notes the greatest mana value of every card put into exile this turn and has that power with a fixed toughness, while its upkeep trigger grants normal-cost play permission for the top card | `b/BellBorcaSpectralSergeant.java` + `ON_ANY_CARD_EXILED` note marker + `GreatestManaValueNotedForSourceThisTurn` + `ExileTopCardMayPlayThisTurnEffect(false)` |
| equipped attack trigger chooses damage to any target or a free instant/sorcery cast from hand capped by attached Equipment mana value | `t/TetsuoImperialChampion.java` + `ConditionalEffect(new Equipped(), new ChooseOneEffect(...))` + `GreatestManaValueAmongAttachedEquipment` |
| artifact gains all activated abilities of lands on the battlefield and may spend mana as any color for them | `m/ManascapeRefractor.java` + STATIC `GainActivatedAbilitiesOfAllLandsEffect()` + `SpendManaAsAnyColorForActivatedAbilitiesEffect()` + `EntersTappedEffect()` |
- chosen creature type, copy each matching nontoken creature entering under your control, temporary hasty copy | `ChooseSubtypeOnEnterEffect` + `TriggeringPermanentConditionalEffect(PermanentAllOfPredicate(PermanentHasSourceChosenSubtypePredicate, PermanentNotPredicate(PermanentIsTokenPredicate)), CreateTokenCopyOfEnteringPermanentEffect(true, true))`
| Aura attachment-count boost plus target-sensitive Equip/Aura cost reductions | `s/StrongBack.java` + `StrongBackTest.java` |
| X-powered ETB puts X +1/+1 counters on itself and destroys any number of target artifacts/enchantments with combined mana value X or less | `r/RampagingYaoGuai.java` + `EnterWithCountersEffect(XValue)` + `DestroyTargetPermanentsWithinTotalManaValueEffect.withinTotalManaValue(...)` |
| playtest artifact that replaces normal turns with sequential phases | `r/RunedTerror.java` + `RunedTerrorEffect` + turn progression phase-cycle support |
| playtest artifact with tap-to-surveil and temporary graveyard animation | `m/Microscope.java` + `AnimateTargetGraveyardCardEffect` |
| global static replacement that makes every creature assign combat damage using mana value | `n/NarodTheBeigeFlower.java` + `AssignCombatDamageWithManaValueEffect(ALL_CREATURES)` |
| playtest legendary banding creature with banding-creature cost reduction and band-size attack boosts | `c/ChatzukMightyGuitarist.java` + `ReduceCastCostForMatchingSpellsEffect` + `BoostCreaturesInAttackingBandsEffect` |
| playtest creature that enters with five custom counters for each Forest or Plant its controller controls | `d/DairyCow.java` + `EnterWithCountersEffect(MILK, Scaled(PermanentCount(Forest or Plant, CONTROLLER), 5))` |
| playtest creature with library-only ninjutsu and a mandatory combat-damage draw | `p/PanglacialShinobi.java` + `LibraryNinjutsuEffect` + `DrawCardEffect` |
| mulligan-time reveal and look at top two | `n/NoRegretsEgret.java` + `NoRegretsEgretEffect` | `MULLIGAN_ACTION`; accepting publicly reveals the card, privately reveals up to two library cards in order, and leaves the normal mulligan decision available |
| playtest Saga whose lore counter is added at the beginning of its controller's end step | `n/NightOfTheFlyingMerfolk.java` + `Card.bedtimeStory` + `SagaChapterService` end-step handling |
| playtest Quest enchantment with three persistent objectives that sacrifice itself and create a token copy of a named creature when all are checked | `m/MapToLorthossTemple.java` + `CompleteChecklistObjectiveEffect` + artifact/permanent-enter and instant-or-sorcery cast triggers |
| static enchantment that lets its controller choose any position in their library for each draw and draws on entry | `h/HeartOfADuelist.java` + `DrawFromAnywhereInLibraryEffect` + `DrawFromLibraryPositionChoice` |
| playtest enchantment that tracks a random ten-digit number across land plays and spell casts, drawing and winning after all digits are crossed | `d/DuelistsConvocationInternational.java` + `DuelistsConvocationInternationalTriggerEffect` + persistent permanent digit state |
| creature whose ETB makes all unblocked creatures attacking its controller become blocked by it and that can block any number of creatures | `n/NobleOx.java` + `CanBlockAnyNumberOfCreaturesEffect` + `MakeAllUnblockedCreaturesAttackingControllerBlockedBySourceEffect` |
| sorcery that creates a token and copies itself for each distinct prior spell or land mana value | `f/Friarball.java` + `CreateTokenEffect` + `ON_SELF_CAST CoststormEffect` |
| creature that names a legendary Dog token on ETB and upkeep, then scales on attack with legendary creatures you control | `a/AGirlAndHerDogs.java` + `CreateTokenWithChosenNameEffect` + `BoostSelfEffect(PermanentCount(...))` |
| spell card that resolves into its owner's command zone and creates a controller-cast life-gain trigger | `e/EssenceOfAjani.java` + `PutResolvingSpellIntoCommandZoneEffect` + `COMMAND_ZONE_ON_CONTROLLER_CASTS_SPELL` |
| creature whose adjacent battlefield neighbors receive a static boost and vigilance | `d/DefenderOfTheQueue.java` + `StaticBoostEffect` + `PermanentAdjacentToSourcePredicate` |
| playtest creature with an as-enters game-long buddy-list choice and a creature-type enter trigger | `c/ChampionOfTheHareish.java` + `BuddyListOnEnterEffect` + `ChampionOfTheHareishTriggerEffect` |
| playtest daybound creature with an optional hand-or-graveyard creature card as a dynamic nightbound back face | `w/Werewhat.java` + `WerewhatOnEnterEffect` + `WerewhatSupport` |

| activated two-card library search with one card to the battlefield tapped and one to hand | `n/NavigationOrb.java` + `SacrificeSelfCost` + `SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect.forCardsMatching(...)` |
| once-per-turn optional spell-cast trigger that bottoms the triggering spell, then reveals until a nonland card for a free cast | `n/NeeraWildMage.java` + `PutTriggeringSpellOnBottomThenRevealEffect` |
| d20 after revealing up to two basic lands from the library | `DruidOfTheEmeraldGrove.java` + `SearchLibraryForUpToTwoBasicLandsThenRollD20Effect` |
| death-time last-known power captured into a source-independent one-time creature-spell perpetual boost boon | `d/DragonbornImmolator.java` + `ConditionalEffect(SourcePowerAtLeast(1), RegisterOneShotCreatureSpellPerpetualPowerBoostEffect)` + `PerpetuallyBoostTriggeringCardEffect` |
| look at four cards, choose one name, take all matching looked-at cards and lose life per card, random-bottom the rest | `s/StrokeOfLuck.java` + `LookAtTopCardsChooseSameNameToHandEffect` |
| target opponent creature perpetually gets -2/-2 and gains an upkeep trigger that perpetually gives it -1/-1 | `s/SewerPlague.java` + `PerpetuallyBoostTargetCreatureEffect` + `PerpetuallyGrantTriggeredAbilityToTargetCreatureEffect` |
| filtered public hand reveal, mandatory discard or fallback draw, and life loss | `m/MindSpike.java` + `MindSpikeEffect` |
| ETB reveals matching creature and land cards from a target opponent's hand, then perpetually makes the chosen card enter tapped | `b/BoareskyrTollkeeper.java` + `RevealMatchingCardsFromTargetHandAndKeepEffect` + `PerpetuallyEnterTappedChosenCardEffect` |
| graveyard-count beginning-of-combat boost with five color-specific specialize faces | `s/SarevokTheUsurper.java` + `SpecializeSarevokEffect` + `SeekLibraryAndConjureDuplicatesInGraveyardEffect` |
| attack-trigger source-power boost with five keyword/untap specialize faces | `s/SkanosDragonVassal.java` + `SpecializeSkanosEffect` |
| target nontoken creature gets +2/+0 and double team, with conditional first strike for a shared creature name or graveyard creature card | `u/UnexpectedAllies.java` + `BoostTargetCreatureEffect` + `GrantKeywordEffect.toTargetIf` + `PermanentSharesNameWithControlledCreatureOrGraveyardCreaturePredicate` |
| ward creature with five specialize faces and perpetual graveyard abilities that target your creatures | `w/WilsonBearComrade.java` + `SpecializeWilsonEffect` + targeted perpetual keyword/static effects |
| ETB compares the entering creature's power with the source's power at resolution and counters the lower-power creature, with equality favoring the source | `s/ShelindaYevonAcolyte.java` + `EnteringCreatureSourcePowerBranchEffect` |
| Attack Mug trigger: each player mills one, land rider creates Treasure, and one exact milled spell may be cast from any graveyard this turn | `l/LockeTreasureHunter.java` + `MugEffect` |
| planar arrival and upkeep token, then targeted-player chaos sacrifice with a toughness-based token rider | `t/TheWilds.java` + `TargetPlayerSacrificesCreatureThenCreateTokensIfToughnessAtLeastEffect` |
| target player sacrifices an attacking creature, then the spell controller creates Soldier tokens equal to its toughness | `e/EntrapmentManeuver.java` + `TargetPlayerSacrificesAttackingCreatureThenCreateTokensEqualToToughnessEffect` |
| optional attack-trigger exile of another attacking creature you control, then reveal a creature onto the battlefield tapped and attacking | `f/FirefluxSquad.java` + `ExileTargetAttackingCreatureThenRevealUntilCreatureToBattlefieldEffect` + `RevealUntilCardPredicateRestOnBottomRandomEffect.tappedAndAttacking(...)` |
| attack trigger offers one artifact spell from hand or graveyard, cast by paying life equal to its mana value | `a/AnrakyrTheTraveller.java` + `MayCastArtifactFromHandOrGraveyardByPayingLifeEqualToManaValueEffect` |
| play lands and cast cards surveilled this turn from your graveyard, paying life equal to a spell's mana value | `e/EyeOfDuskmantle.java` + `CastSurveilledCardsFromGraveyardByPayingLifeEffect` |
| spell costs less for each distinct graveyard mana value and discard trigger casts the exact discarded card | `o/OskarRubbishReclaimer.java` + `ReduceOwnCastCostEffect(new DistinctManaValuesAmongCardsInGraveyard())` + `CastDiscardedCardFromGraveyardEffect` |
| ETB registers an end-of-turn ally nontoken-death trigger that creates one token per dying creature power | `i/InfestedThrinax.java` + `RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect(ON_ALLY_NONTOKEN_CREATURE_DIES, CreateTokenEffect(new EventValue(), ...))` |
| one-or-more ally creatures with base P/T 1/1 enter; attack boosts other base 1/1 creatures by source counters | `b/BessSoulNourisher.java` + `ON_ALLY_CREATURES_ENTERS_BATTLEFIELD TriggeringPermanentConditionalEffect` + filtered `BoostAllOwnCreaturesEffect` |
| dynamic power from greatest creature-card power in all graveyards + attack mill + one land/one spell from cards milled this turn | `c/CoramTheUndertaker.java` + `DynamicStaticBoostEffect(GreatestPowerAmongCardsInGraveyard)` + `ON_ATTACK MillEffect` + `PlayLandAndCastSpellFromCardsPutIntoGraveyardsFromLibrariesThisTurnEffect` |
| combat-damage mill that many with an optional any-number milled-land battlefield return + +10/+10 per recorded game loss | `r/RampantFrogantua.java` + `DynamicStaticBoostEffect(Scaled(PlayersWhoLostGame, 10))` + `MayEffect(MillControllerAndMayPutMilledLandsOntoBattlefieldEffect(EventValue))` |
| cast-time copy for each distinct counter kind among controlled permanents | `s/StormOfForms.java` + `CopyThisSpellForEachCounterKindEffect` |
| ETB optionally puts a chosen counter on itself for each distinct counter kind among controlled permanents | `b/BribeTaker.java` + `ChooseCounterForEachControlledCounterKindEffect` |
| attack trigger chooses a counter on a controlled permanent and copies it to a targeted controlled permanent | `a/AvenCourier.java` + `ChooseCounterTypeOnControlledPermanentThenPutOnTargetPermanentEffect` |
| each player chooses a nontoken creature starting with the spell controller, all chosen creatures are sacrificed, then a d20 plus the controller's sacrificed creature's toughness returns one or up to two of those creature cards | `d/DanseMacabre.java` + `DanseMacabreEffect` + `RollD20Effect.withAddedAmount` |
| attack trigger puts an egg counter on another target creature and watches that exact permanent's death | `x/XiraTheGoldenSting.java` + `PutCounterAndWatchTargetCreatureDeathEffect(EGG, restriction, SequenceEffect.of(DrawCardEffect(), CreateTokenEffect(...)))` |
| creature-entry trigger may move one chosen counter from this artifact onto that creature | `a/AgentsToolkit.java` + `MoveChosenCounterFromSourceToEnteringCreatureEffect` |
| targeted ally-creature ETB opponent life loss equal to entering creature's absolute power/toughness difference | `j/JawsOfDefeat.java` + `TargetOpponentLosesLifeEqualToPowerToughnessDifferenceEffect` |
| instant/sorcery cast trigger that creates a typed token, then checks a controlled subtype count for a temporary mass base-P/T change | `l/LordOfTheNazgL.java` | `SpellCastTriggerEffect` + `CreateTokenEffect` + `ConditionalEffect(ControlsPermanentCount, SetAllOwnCreaturesBasePowerToughnessEffect)` |
| single-copy trigger for a spell targeting one opponent's nonland permanent, choosing a legal nonland permanent controlled by another opponent | `e/ExterminatorMagmarch.java` + `CopySpellForAnotherOpponentPermanentEffect` |
| artifact tracks resolving instant/sorcery spells in source-linked exile, then sacrifices to offer any number of those spells for free within an X-based total mana-value cap | `r/RodOfAbsorption.java` | `SpellCastTriggerEffect` + `ExileTriggeringSpellWithSourceEffect` + `CastSpellsExiledWithSourceWithinTotalManaValueEffect` |
| controlled subtype creatures gain protection from the current Ring-bearer | `l/LordOfTheNazgL.java` | `GrantEffectEffect(new ProtectionFromRingBearersEffect(), GrantScope.ALL_OWN_CREATURES, filter)` |
| ETB draft from a spellbook, then perpetually grant the card a Food artifact type and sacrifice-for-life ability | `h/HinterlandChef.java` + `PerpetuallyGrantCardCharacteristicsEffect` |
| non-hand spell-cast trigger plus a tapped mana/discard activation that exiles until a nonland and offers it for free | `n/NicoMinoruRunaway.java` + `SpellCastTriggerEffect` + `ExileTopUntilNonlandMayCastWithoutPayingManaEffect` |
| ETB draft that gives the chosen card perpetual any-color casting and a self-cast bounce trigger | `o/OminousTraveler.java` + `DraftCardFromSpellbookEffect(..., chosenCardEffects)` + `PerpetuallyGrantAnyColorManaAndSelfCastAbilityToCardEffect` |
| upkeep draft from a spellbook, exile the choice, and grant end-of-turn play permission | `a/ArmsScavenger.java` + `DraftCardFromSpellbookEffect(..., true)` + `ReduceEquipCostEffect(1)` |
| planeswalker with a perpetual hand-card choice, spellbook battlefield draft, and mass trample pump | `g/GarrukWrathOfTheWilds.java` + `ChooseCardFromHandAndApplyPerpetualPowerToughnessAndCostReductionEffect` + `DraftCardFromSpellbookEffect(..., false, true)` |
| random opponent gains control of source permanent | EFFECTS_QUICK_REFERENCE.md and EFFECTS_INDEX.md |
| each opponent chooses between a free cast from their library and four-card total-mana-value damage | `e/EnsnaredByTheMara.java` + `EnsnaredByTheMaraVillainousChoiceEffect` |
| villainous-choice replacement plus additional vote | `t/TheValeyard.java` + `TheValeyardEffect` |
| draw three, then target opponent chooses between discarding three and a controller free-cast from hand | `g/GreatIntelligencesPlan.java` + `GreatIntelligencesPlanVillainousChoiceEffect` |
| randomly choose a creature, steal it until end of turn, untap and haste it, then destroy the other creatures | `t/TheNiptonLottery.java` + `ChooseRandomCreatureGainControlUntilEndOfTurnThenDestroyOtherCreaturesEffect` |
| command-zone secret mission with end-step threshold, optional reveal/reward, and one-shot face-down completion | `m/MarchesasSurpriseParty.java` + `COMMAND_ZONE_END_STEP_TRIGGERED` + `ConditionalEffect(AnyOf(...), MayEffect(SequenceEffect.of(TurnFaceDownCommandZoneCardEffect, DrawCardEffect(1))))` |
| command-zone conspiracy restricting attacks to an even number and doubling a target creature's power | `r/RuleWithAnEvenHand.java` + `COMMAND_ZONE_STATIC CombatAttackCountRestrictionEffect` + `COMMAND_ZONE_ON_ALLY_CREATURES_ATTACK` |
| playtest creature with custom nimble evasion and thoughtweft keyword sharing | `b/BrigidWhosSeenSomeStuff.java` + filtered `GrantKeywordEffect` |
| playtest Sliver lord with a custom damage-prevention keyword | `s/SliverOfHope.java` + filtered `GrantKeywordEffect(Keyword.HOPE)`; Hope is enforced by the damage pipeline for attacking creatures |
| council's dilemma vote for wild or free, revealing creatures and putting permanents from hand onto the battlefield | `s/SelvalasStampede.java` + `SelvalasStampedeEffect` |
| scheme creates a nonlegendary token copy of the controller's commander from any zone | `CreateTokenCopyOfCommanderEffect` |
| scheme temporarily steals opponents' nonland permanents, then untaps, hastes, and forces creatures to attack their owners | `m/MyCrushingMasterstroke.java` + `GainControlOfAllPermanentsMatchingEffect(..., END_OF_TURN, UntapPermanentsEffect(TARGET), GrantKeywordEffect(HASTE, TARGET), PermanentMustAttackItsOwnerThisTurnEffect)` |
| perpetually grant a triggered ability to a card that later leaves the graveyard | `o/OglorDevotedAssistant.java` and EFFECTS_QUICK_REFERENCE.md |
| ETB perpetually grants a death trigger to current creatures you control | `a/AntiqueCollector.java` and EFFECTS_QUICK_REFERENCE.md |
| perpetually grant a graveyard-activated ability to a targeted creature card | `a/AssembleFromParts.java` and EFFECTS_QUICK_REFERENCE.md |
| perpetually grant a keyword to matching permanents and cards in hand | `r/RimewallProtector.java` and EFFECTS_QUICK_REFERENCE.md |
| perpetually grant a death-to-exile replacement to opposing creatures and planeswalkers | `b/BrittleBlast.java` and EFFECTS_QUICK_REFERENCE.md |
| damage each creature, then choose an instant or sorcery in hand for a perpetual noncombat-damage bonus | `c/ConductiveCurrent.java` and EFFECTS_QUICK_REFERENCE.md |
| becomes blocked by a creature and perpetually reduces that blocker's power | `w/WizenedGithzerai.java` + `PerpetuallyBoostCombatOpponentEffect` |
| target creature power damage, then perpetually boost a creature card by excess damage | `r/RavenousPursuit.java` and `TargetCreatureDealsPowerDamageToTargetCreatureThenApplyPerpetualPowerToughnessEffect` |
| ETB exiles one instant or sorcery from each eligible graveyard and casts one random copy if at least two were exiled | `m/MysteriousStranger.java` + `ExileGraveyardInstantsOrSorceriesAndCastCopiesEffect.forRandomSingleCopy()` |
| target creature power damage, then create Elf Warrior tokens for excess damage | `w/WindswiftSlice.java` and `TargetDealsPowerDamageToTargetEffect.recordingExcessDamage()` followed by `CreateTokenEffect(new EventValue(), ...)` |
| destroy all creatures, then perpetually boost every creature card in hand | `b/BeginAnew.java` + `PerpetuallyBoostMatchingHandCardsEffect` |
| ETB perpetually boosts creature cards in hand and specializes into five color-specific faces | `k/KlementNoviceAcolyte.java` + `PerpetuallyBoostMatchingHandCardsEffect` + `SpecializeKlementEffect` |
| cast-granted opponent-target trigger plus five specialize faces | `l/LaezelGithyankiWarrior.java` + `SpecializeLaezelEffect` + `GrantTriggeredAbilityToCastSpellEffect` |
| attack trigger with an optional attacking-creature target, next-end-step bounce, and five specialize riders | `a/AloraRogueCompanion.java` + `RegisterDelayedEndStepTriggerEffect` + `SpecializeAloraEffect` |
| instant-or-sorcery controller spell-cast triggers with five specialize faces, including a perpetual static anthem | `g/GaleConduitOfTheArcane.java` + `SpecializeGaleEffect` + `PerpetuallyGrantStaticEffectToSourceEffect` |
| conditional-cost five-face specialize creature with an attacking-alone evasion clause and optional graveyard exile reflexive triggers | `i/ImoenTricksterFriend.java` + `SpecializeImoenEffect` + `ExileOwnGraveyardCardThenEffect` |
| study-counter loot creature with five dynamic specialization faces | `v/VhalEagerScholar.java` + `SpecializeVhalEffect` + `SeekTwoCardsToBattlefieldWithinManaValueEffect` |
| specialize creature with artifact/enchantment hexproof, an optional destruction target, five color riders, and a one-shot perpetual spell boon | `j/JaheiraHarperEmissary.java` + `SpecializeJaheiraEffect` + `PerpetuallyBoostTriggeringCardEffect` |
| life-loss trigger creature with symmetric end-step damage and five specialization faces | `s/ShadowheartSharranCleric.java` + `SpecializeShadowheartEffect` + conditional controller-life-loss triggers |
| ETB optional fight with source-tracked exile replacement and five token-copy specialize faces | `g/GutFanaticalPriestess.java` + `SpecializeGutEffect` + `CreateTokenCopyOfExiledCreatureWithSourceEffect` |
| battlefield-or-graveyard activated five-face specialize creature with perpetual no-block and color-specific riders | `k/KarlachRagingTiefling.java` + `SpecializeKarlachEffect` + `SeekInstantOrSorceryAndMayCastFreeEffect` |
| temporary-control ETB with mana-value restriction and five sacrifice-based specialize faces | `w/WyllPactBoundDuelist.java` + `SpecializeWyllEffect` + `DrawCardsUnlessTargetPaysLifeEffect` |
| six-land five-face specialize creature with color-form static abilities and death-time unspecialization | `l/LukaminaMoonDruid.java` + `SpecializeLukaminaEffect` + `UnspecializeLukaminaEffect` |
| conjure a random creature duplicate from an opponent's library with perpetual casting permission | EFFECTS_QUICK_REFERENCE.md and EFFECTS_INDEX.md |
| optionally exile a creature from hand, exile any number of same-named cards from hand and library, then conjure duplicates of a chosen outside-game creature | `g/GrizzledHuntmaster.java` + `GrizzledHuntmasterEffect` |
| conjure named cards into your library, then shuffle | `t/ToralfsDisciple.java` + `ConjureCardNamedIntoLibraryEffect` |
| discard-triggered conjure at a fixed position from the top of your library | `c/CalimDjinnEmperor.java` + `ConjureCardIntoLibraryAtPositionEffect` |
| conjure duplicates of up to two target nontoken creatures you control into your hand | `ConjureDuplicateOfTargetCreatureIntoHandEffect` + `target(new ControlledPermanentPredicateTargetFilter(...), 0, 2)` |
| target opponent sacrifices a nontoken creature, then a small one is conjured into your hand with perpetual any-color casting | `GraveChoice.java` + `TargetPlayerSacrificesNontokenCreatureThenConjuresDuplicateEffect` |
| conjure duplicates of X target nontoken permanents into your hand, with an optional X>=5 battlefield choice | `SnowbornSimulacra.java` + `ConjureDuplicatesOfTargetPermanentsIntoHandEffect` + `PutChosenCardFromHandOntoBattlefieldEffect` |
| destroy up to three target artifacts, then conjure modified duplicates of nontoken artifacts destroyed | `FlamesOfMoradin.java` + `DestroyTargetArtifactsThenConjurePerpetualCopiesIntoHandEffect` |
| conjure a duplicate of a card returned from your graveyard to your hand | `v/VeteranGhoulcaller.java` + `ConjureDuplicateOfCardReturnedFromGraveyardToHandEffect` |
| seek nonland cards and apply a perpetual hand cost reduction | EFFECTS_QUICK_REFERENCE.md and EFFECTS_INDEX.md |
| seek a basic land onto the battlefield, then seek an exact-mana-value permanent to hand | `s/SettleTheWilds.java` + `SeekCardToBattlefieldEffect` + `SeekCardsToHandEffect` |
| ETB seeks exact-mana-value spells into source-tracked exile, then upkeep copies one for free | `s/SignatureSpells.java` + `SeekLibraryEffect(..., EXILE_WITH_SOURCE, ManaValueBound)` + `CopyCardsExiledWithSourceAndMayCastCopiesEffect(false, FREE)` |
| council's dilemma vote with one basic-land search or graveyard return per vote | `t/TravelThroughCaradhras.java` + `TravelThroughCaradhrasEffect` |
| council's dilemma vote with a majority graveyard return or tied/embark hand refresh | `s/SailIntoTheWest.java` + `SailIntoTheWestEffect` |
| secret player vote, vote-counted draws, and zero-vote permanent cards from hand | `c/CirdanTheShipwright.java` + `CirdanTheShipwrightEffect` |
| secret player vote, damage to each opponent for each vote, and draw for votes received | `m/MobVerdict.java` + `MobVerdictEffect` |
| secret opponent choices with revealed all-same or mixed damage branches | `p/PrisonersDilemma.java` + `PrisonersDilemmaEffect` |
| post-vote trigger that compares the controller's choices with each opponent's choices | `e/ErestorOfTheCouncil.java` + `ErestorOfTheCouncilEffect` + `VotingResult` |
| post-vote trigger that offers the controller and matching voters a may-scry ability | `m/ModelOfUnity.java` + `ModelOfUnityEffect` + `VotingResult` |
| exile up to one target creature card from your graveyard, then seek a creature with mana value one higher and perpetually grant menace | `p/PuppetRaiser.java` and `ExileTargetCreatureCardFromGraveyardThenSeekWithMenaceEffect` |
| Gift token + draw/Seek replacement | `p/PoolResources.java` + `GiftEffect` + `SeekLibraryEffect` |
| end-step tapped-creature-count Seek to battlefield | `b/BuxtonDecoratedHost.java` + `ConditionalEffect` + `SeekLibraryEffect` + `ManaValueBound` |
| targeted hand exile + threshold Rat Seek with perpetual cost reduction | `t/ThoughtRattle.java` + `ChooseCardsFromTargetHandEffect` + `ConditionalEffect` + `SeekLibraryAndPerpetuallyReduceSoughtCardEffect` |
| target opponent chooses X cards from hand, then controller may cast one selected spell for free | `e/ExtractBrain.java` + `TargetPlayerChoosesCardsFromHandThenMayCastOneEffect` |
| death-triggered creature Seek with perpetual haste, cost reduction, and end-step sacrifice | `g/GoblinTrapfinder.java` + `SeekLibraryAndPerpetuallyModifySoughtCardEffect` |
| exile opposing low-mana permanent, then owner seeks shared card type on source leaves | `d/DarkstarBanisher.java` + `ExileTargetPermanentAndTrackWithSourceEffect` + `SeekLibraryForOwnerOfCardExiledWithSourceEffect` |
| attack counter drives face-down exact-mana-value seek; death returns and later discards the returned cards | `k/KardumPatronOfFlames.java` + `SeekLibraryEffect(..., EXILE_WITH_SOURCE, ManaValueBound)` + `PutAllCardsExiledWithSourceIntoOwnersHandsAndDiscardAtNextTurnEndStepEffect` |
| perpetual offspring grant to a hand card | EFFECTS_QUICK_REFERENCE.md and EFFECTS_INDEX.md |
| perpetual cast-life-loss grant to nonland cards in the defending player's hand | `p/PutrefyingRotboar.java` + `PerpetuallyGiveSpellCastLifeLossToDefendingHandEffect` |
| perpetual extra mana ability grant to the topmost land card in your library | `v/VigorousFarming.java` + `PerpetuallyGiveLandExtraManaEffect` |
| perpetual random graveyard permanent conversion into a playable Food artifact | `r/ResourcefulCollector.java` + `PerpetuallyMakeRandomGraveyardPermanentFoodEffect` |
| perpetual cost reduction grant to instant and sorcery cards in hand | `c/ChargedConjuration.java` + `PerpetuallyReduceInstantAndSorceryCastCostInHandEffect` |
| perpetual cost reduction grant to creature cards in hand | `f/FountainportCharmer.java` + `PerpetuallyReduceCreatureCastCostInHandEffect` |
| perpetual +1/+1 grant to creature cards in your library | `b/BramblearmorBrawler.java` + `PerpetuallyBoostCreatureCardsInLibraryEffect` |
| perpetual +1/+1 grant to an enter-trigger source and the entering creature | `l/LeafLeapGuide.java` + `PerpetuallyBoostSourceAndEnteringCreatureEffect` |
| perpetual keyword grant to an enter-trigger source | `m/MarshlandHordemaster.java` + `PerpetuallyGrantKeywordToSourceEffect` |
| once-per-turn non-flying creature cast duplicate with perpetual flying | `a/AceFlockbringer.java` + `ConjureDuplicateOfTriggeringCreatureToHandEffect` |
| one-time boon that makes the next non-flying creature spell perpetually gain flying | `l/LuluForgetfulHollyphant.java` + `RegisterDelayedControllerSpellCastTriggerEffect.oneShotUntilConsumed` + `PerpetuallyGrantKeywordsToTriggeringCardEffect` |
| Gift a named card onto an opponent's battlefield | `a/ArchivalWhorl.java` + `ConjureCardToOpponentBattlefieldEffect` |
| ETB forage conjures a named card onto your battlefield; batched Squirrel combat damage may sacrifice a token to add acorn counters | `e/EuruAcornScrounger.java` + `ConjureCardToBattlefieldEffect` |
| Rat-triggered named card conjured into a graveyard | `s/ShellfishScholar.java` + `ConjureCardToGraveyardEffect` |
| ETB named card conjured into your hand | `b/BraveMeadowguard.java` + `ConjureCardToHandEffect` |
| attack-triggered named card conjured into your library with perpetual abilities | `s/SanguineSoothsayer.java` + `ConjureCardIntoControllerLibraryEffect` |
| activated spellbook choice that conjures one named card into hand | `c/ChargedConjuration.java` + `ConjureCardFromSpellbookToHandEffect` |
| attack-triggered draft of three random spellbook cards into hand | `r/RecruitInstructor.java` + `DraftCardFromSpellbookToHandEffect` |
| batched combat-damage trigger that randomly conjures a spellbook card into playable exile | `d/DazzlingFlameweaver.java` + `ConjureRandomCardFromSpellbookToExileMayPlayUntilNextTurnEffect` |
| ally creature enters if it was cast; sacrifice it and conjure a perpetually modified duplicate | `p/PrototypeX8.java` | `ON_ALLY_CREATURE_ENTERS_BATTLEFIELD TriggeringPermanentConditionalEffect(PermanentCastBySourceControllerThisTurnPredicate, SacrificeTriggeringPermanentThenConjureDuplicateEffect(...))` |
| perpetually gain selected keywords of another creature that enters under your control | `m/MutablePupa.java` | `ON_ALLY_CREATURE_ENTERS_BATTLEFIELD PerpetuallyGainKeywordsOfTriggeringCreatureEffect()` |
| landfall perpetually grants a random library land a tap-draw trigger | `a/AmbassadorOfEvendo.java` | `ON_ALLY_LAND_ENTERS_BATTLEFIELD PerpetuallyGrantTapDrawToRandomLandInLibraryEffect()` |
| conjure a named card into the top N cards of a library with a perpetual casting option | EFFECTS_QUICK_REFERENCE.md and EFFECTS_INDEX.md |
| choose an opponent, then you and that player each create tokens | `ChooseOpponentEachCreatesTokensEffect` |
| each opponent draws a card, then the controller draws for each opponent who drew | `EachOpponentDrawsThenControllerDrawsEffect()` |
| on leaving, choose an opponent with more lands and fetch Plains equal to the land-count difference | `ChooseOpponentThenSearchLandDifferenceEffect(CardSubtype.PLAINS)` |
| beginning-of-combat opponent choice remembered for a static menace grant to creatures attacking that player | `t/TriarchStalker.java` + `ChooseOpponentForTargetingRelayEffect` + `PermanentIsAttackingRememberedPlayerPredicate` |
| upkeep creates tokens for each opponent meeting a hand-size threshold | `CreateTokenEffect(new PlayersWithCardsInHandAtLeast(CountScope.OPPONENTS, threshold), ...)` |
| attack trigger scales a defending-player sacrifice count by that player's poison counters | `k/KozilekCompleated.java` + `DefendingPlayerPoisonCounters` + `SacrificePermanentsEffect(..., DEFENDING_PLAYER)` |
| ETB flips once for each opponent, resolving a separate win/loss branch for that opponent | `ON_ENTER_BATTLEFIELD FlipCoinForEachOpponentEffect(winEffect, lossEffect)` |
| any number of target opponents; create one token for each creature they control | `CreateTokensForEachTargetPlayerCreatureEffect` + `target(opponent, 0, 99)` |
| upkeep: each opponent chooses one of three results; controller and chooser share the result | `EachOpponentChoosesMasterOfCeremoniesEffect` |
| each player chooses friend or foe; friends copy a creature they control, foes return one | `ZndrsplatsJudgmentEffect` |
| each opponent chooses fame or fortune; controller chooses a creature for each fame choice, or draws and creates a Treasure for each fortune choice | `SeizeTheSpotlightEffect` |
| Vehicle ETB: each player chooses up to two nontoken, non-Vehicle creatures to exile until it leaves; attack trigger puts one exiled card into its owner's graveyard and investigates | `f/ForebodingSteamboat.java` + `EachPlayerChoosesOwnPermanentsToExileUntilSourceLeavesEffect` + `PutTargetCardExiledWithSourceIntoOwnersGraveyardThenInvestigateEffect` |
| target an opponent, then have that opponent choose a player for a temporary global cast/attack restriction | `TargetOpponentChoosesPlayerForRestrictionEffect` + `PlayerCantCastSpellsAndAttackWithCreaturesEffect` |
| end step may return another creature you control, then put counters on the source equal to its power | `f/FirstResponder.java` + `ReturnCreatureToHandAndPutCountersOnSourceEqualToPowerEffect` |
| end step draws and creates Treasures for opponents meeting independent turn thresholds | `s/SmugglersShare.java` |
| upkeep reveals a fresh top card for each opponent, who may pay its mana value in life to exile it | `p/ProtectionRacket.java` + `ProtectionRacketEffect` |

- Lich (2ED 114): `LoseLifeEqualToLifeTotalAsEntersEffect` on `ON_ENTER_BATTLEFIELD`; static `CantLoseGameFromLifeEffect` and `NefariousLichLifeGainReplacementEffect`; `SacrificePermanentsOrLoseGameEffect(EventValue, not-token)` on `ON_CONTROLLER_DEALT_DAMAGE`; `ControllerLosesGameEffect` on `ON_DEATH`. Entry life loss is a replacement, damage sacrifice is triggered, and only a battlefield-to-graveyard departure triggers the explicit loss.

Purpose: quickly find a reference card for the pattern you're implementing. One or two examples per archetype. All paths relative to `cards/`.

| as-enters number choice, noncreature spells with chosen mana value can't be cast | `TalionTheKindlyLord` + `NoncreatureSpellsWithChosenManaValueCantBeCastEffect` |

This index has been split into smaller files for faster lookup. Each file is under 10k tokens.

## File index

| File | Sections | When to use |
|------|----------|-------------|
| [CARD_PATTERNS_LANDS_SPELLS.md](CARD_PATTERNS_LANDS_SPELLS.md) | Lands, Spells | Implementing lands (basic, pain, check, fast, creature, utility) or spells (burn, pump, destroy, board wipe, draw, mill, counterspell, modal, graveyard, steal, extra turn) |
| [CARD_PATTERNS_CREATURES_ETB.md](CARD_PATTERNS_CREATURES_ETB.md) | Vanilla, Keyword, ETB creatures | Implementing creatures with no abilities, keyword-only creatures, or ETB triggers |
| [CARD_PATTERNS_CREATURES_TRIGGERED.md](CARD_PATTERNS_CREATURES_TRIGGERED.md) | Triggered creatures | Implementing creatures with triggered abilities (attack, block, death, damage, upkeep, draw, spell cast, graveyard) |
| [CARD_PATTERNS_PERMANENTS_STATIC.md](CARD_PATTERNS_PERMANENTS_STATIC.md) | Static permanents, Auras | Implementing lords/anthems, static restrictions, auras (lockdown, boost, curse) |
| [CARD_PATTERNS_PERMANENTS_ARTIFACTS.md](CARD_PATTERNS_PERMANENTS_ARTIFACTS.md) | Artifacts, Vehicles, Equipment | Implementing artifacts, vehicles, or equipment |
| [CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md](CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md) | Activated abilities, Planeswalkers, Sagas | Implementing activated abilities (tap, sacrifice, mana, pump), planeswalkers, or sagas |
| [CARD_COPY_PASTE_TEMPLATES.md](CARD_COPY_PASTE_TEMPLATES.md) | Full card + test templates | Ready-to-use copy-paste templates for the most common archetypes (pump, burn, draw, destroy, ETB, counter, aura, aura with ability). Use line offsets from its quick selector table. |

## Quick pattern-to-file lookup

| Pattern keyword | File |
|----------------|------|
| d20, roll a d20, graveyard target ETB | CARD_PATTERNS_CREATURES_ETB.md |
| whammy deck, reveal until Island, choose to stop | EFFECTS_QUICK_REFERENCE.md and EFFECTS_INDEX.md |
| reveal until creature to hand, bottom the rest randomly, and put +1/+1 counters on a target creature equal to the found creature's mana value | `y/YunasWhistle.java` + `SequenceEffect.of(RevealUntilCardPredicateRestOnBottomRandomEffect.toHandRecordingManaValue(...), PutCounterOnTargetPermanentEffect(PLUS_ONE_PLUS_ONE, EventValue()))` |
| land, basic, pain, check, fast, manland | CARD_PATTERNS_LANDS_SPELLS.md |
| burn, damage, shock, bolt, X burn | CARD_PATTERNS_LANDS_SPELLS.md |
| pump, boost, giant growth, overrun | CARD_PATTERNS_LANDS_SPELLS.md |
| destroy, terror, wrath, board wipe, total power and toughness target restriction | CARD_PATTERNS_LANDS_SPELLS.md |
| each player chooses a party, then sacrifices the rest | `EachPlayerChoosesPartyThenSacrificesRestEffect` |
| each player sacrifices artifacts, enchantments, and nonbasic lands, then searches for basics | WaveOfVitriolEffect |
| draw, mill, discard, tutor, search | CARD_PATTERNS_LANDS_SPELLS.md; CARD_PATTERNS_PERMANENTS_STATIC.md for static draw replacements |
| each player may discard and draw, then damage accepting opponents | `s/Snort.java` + `EachPlayerMayDiscardHandThenDrawEffect` + `DealDamageToPlayersEffect.selectedOpponents` |
| spellbook, draft from a spellbook, digital card offer | `DraftCardFromSpellbookEffect` + shared `LibraryRevealChoice` + `PerpetuallyMakeSelectedSpellbookCardArtifactCreatureEffect` (`y/SupportSkyforge.java`, YDFT 26) |
| seek a card and discard that exact card later | `SeekLibraryToHandAndRegisterDiscardAtNextEndStepEffect` + `DiscardSpecificCardEffect` |
| opponent searches library, control search choices, exile found cards | CARD_PATTERNS_PERMANENTS_STATIC.md |
| look at top cards, plot from library | CARD_PATTERNS_LANDS_SPELLS.md |
| seek a random matching card from the top of a library, then shuffle | `SeekFromTopOfLibraryEffect` |
| seek a random matching card from the full library into hand, then shuffle | `SeekFromLibraryToHandEffect` |
| seek a random matching card from the full library into the graveyard, then shuffle | `SeekFromLibraryToGraveyardEffect` |
| discard a card, then seek and exile a random card with greater mana value that you may play until the end of your next turn | `DiscardCardThenEffect` + `SeekFromLibraryWithGreaterManaValueEffect` |
| trigger from the card actually drawn without revealing it | `DrawnCardTriggerEffect` |
| random card from a fixed spellbook into hand | `ConjureGoblinInfluxArraySpellbookEffect` or `ConjureSkywriterDjinnSpellbookEffect` |
| landfall draft of three cards from a fixed spellbook into hand | `DraftSlimefootThallidTransplantSpellbookEffect` |
| reveal target opponent's hand, exile a chosen nonland card, then conjure a named card into that player's hand | `ChooseCardsFromTargetHandEffect(..., HandChoiceDestination.EXILE).withChosenCardThen(null, new ConjureCardIntoTargetPlayerHandEffect(card))` |
| random Dragon card from a fixed spellbook into face-down exile with egg counters | `ConjureDarigaazShivanChampionSpellbookEffect` |
| draft three random cards from a fixed spellbook and exile the chosen card tracked to the source | `DraftProteanWarEngineSpellbookEffect` |
| optional enlist trigger that conjures a perpetual duplicate into the top five | `ConjureDuplicateOfEnlistedNontokenCreatureEffect` + `ConjureDuplicateOfEnlistedCreatureIntoTopFiveEffect` |
| next instant/sorcery cast conjures a duplicate into hand, with an un-kicked delayed discard | `RegisterDelayedControllerSpellCastTriggerEffect` + `ConjureDuplicateOfTriggeringSpellIntoHandEffect` + `DiscardSpecificCardEffect` |
| one or more other nontoken creatures deal combat damage, then choose one and conjure its duplicate into hand | `ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER` + `AllyCombatDamageTriggerEffect(..., oncePerDamageStep=true)` + `ConjureDuplicateOfChosenCombatDamageDealerIntoHandEffect` |
| one or more creatures deal combat damage, create a Treasure, then manifest the damaged player's top card under your control | `ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER` + `AllyCombatDamageTriggerEffect(null, SequenceEffect.of(CreateTokenEffect.ofTreasureToken(1), ManifestTopCardOfDamagedPlayerLibraryEffect()), false, true)` |
| perpetually modify a physical card's power and toughness | `PerpetuallyBoostCardEffect` + `GameData.perpetualCardPowerToughnessModifiers` |
| perpetually boost other own creatures and matching creature cards in hand of a chosen type | `ChooseSubtypeOnEnterEffect` + `GrantChosenSubtypeToOwnCreaturesEffect.toSelf()` + `PerpetuallyBoostOtherOwnCreaturesAndHandCreatureCardsOfChosenSubtypeEffect` |
| perpetually boost a Vehicle when a matching creature crews it | `ON_ALLY_CREATURE_CREWS_VEHICLE` + `PerpetuallyBoostVehicleWhenMatchingCreatureCrewsEffect` |
| choose a nonland hand card that perpetually costs less to cast | `ChooseCardFromHandToPerpetuallyReduceCastCostEffect` + `GameData.perpetualCardCastCostReductions` |
| target opponent chooses X cards from hand; look at them and may cast one revealed spell for free | `e/ExtractBrain.java` + `RevealCardsChooseOneToCastEffect` |
| target a creature card in your graveyard, make it perpetually only one card type, and let you cast it this turn | `GrantTargetGraveyardCardCastEffect` + `PerpetuallySetTargetCreatureCardTypeEffect` |
| look at top seven cards, perpetually gain keywords | `p/PriestOfPossibility.java` and `LookAtTopSevenAndPerpetuallyGainKeywordsEffectHandler` |
| exile top cards, play this turn, unplayed exiled cards to graveyard and tokens | `g/GlimpseTheImpossible.java` |
| target opponent's library until instant/sorcery/creature, free-cast with creature haste and end-step sacrifice | `s/StragoAndRelm.java` + `RevealTopCardsOfTargetPlayerUntilInstantOrSorceryAndCastEffect(CardPredicate, true, true)` |
| exile top X cards, free-cast one instant or sorcery with mana value X or less, put uncast instants/sorceries into hand and the rest on the bottom randomly | `m/MuseVortex.java` + `ExileTopCardsAndMayCastSpellsEffect.controllerWithRandomBottomAndMatchingRestToHand(...)` |
| opponent-owned exile count + subtype-gated ETB exile-until-land | `u/UmbrisFearManifest.java` |
| double any effect that doubles, quadruple | EFFECTS_QUICK_REFERENCE.md and ORACLE_TEXT_EFFECT_MAP.md |
| counter, counterspell, cancel | CARD_PATTERNS_LANDS_SPELLS.md |
| counter a spell, then may free-cast one eligible own-graveyard spell at or below its mana value | `c/Counterpoint.java` + `CounterSpellAndMayCastCardFromGraveyardWithTargetSpellManaValueEffect` |
| counter target spell; creature spells perpetually get -2/-0; counter unless pays | CARD_PATTERNS_LANDS_SPELLS.md |
| remove any number of counters from among permanents | CARD_PATTERNS_LANDS_SPELLS.md |
| proliferate, then phase out permanents that received counters | `r/RipplesOfPotential.java` + `PhaseOutPermanentsThatReceivedCountersThisWayEffect` |
| bounce, unsummon, return to hand | CARD_PATTERNS_LANDS_SPELLS.md |
| choose a creature type, return all other creatures to hand | `ReturnAllCreaturesExceptChosenTypeToHandEffect` + resolution-time creature-type choice; `r/RaiseThePalisade.java` |
| graveyard return, reanimate, flashback | CARD_PATTERNS_LANDS_SPELLS.md |
| each opponent chooses a creature from their graveyard and the chosen cards enter under your control | `d/DredgeTheMire.java` + `EachOpponentChoosesCreatureCardFromTheirGraveyardToBattlefieldEffect` |
| targeted opponent-graveyard reanimation followed by exiling that player's graveyard | `n/NurglesConscription.java` + `ExileGraveyardOfTargetCardOwnerEffect` |
| exile any number of graveyard cards with a collective card-type threshold, then return a permanent from among them with a counter | `w/WinterCynicalOpportunist.java` + `ExileAnyNumberOfOwnGraveyardCardsWithFourCardTypesThenPutPermanentOntoBattlefieldEffect` |
| target player's graveyard to bottom in random order | CARD_PATTERNS_CREATURES_ETB.md |
| modal, choose one, fight, bite | CARD_PATTERNS_LANDS_SPELLS.md |
| beginning-of-combat modal with a commander-enabled second mode and up-to-two non-targeting counter recipients | `s/SOLDIERMilitaryProgram.java` | commander-gated `ChooseOneEffect` / `ChooseOneEffect.oneOrMore` plus `PutCounterOnChosenPermanentsEffect` |
| Case, solve, solved | CARD_PATTERNS_PERMANENTS_STATIC.md |
| steal, threaten, extra turn | CARD_PATTERNS_LANDS_SPELLS.md |
| directional multiplayer creature-choice control spell | CARD_PATTERNS_LANDS_SPELLS.md and EFFECTS_QUICK_REFERENCE.md |
| auction, bid life | CARD_PATTERNS_LANDS_SPELLS.md |
| tempting offer, each opponent may accept an effect | EFFECTS_QUICK_REFERENCE.md and ORACLE_TEXT_EFFECT_MAP.md |
| tempting offer, copy target instant or sorcery and copy again for each accepting opponent | `t/TemptWithMayhem.java` + `TemptingOfferCopySpellEffect()` |
| vanilla, no abilities, empty body | CARD_PATTERNS_CREATURES_ETB.md |
| keyword creature, flying, haste, infect | CARD_PATTERNS_CREATURES_ETB.md |
| creature becomes target of an opponent's spell or ability, targeted trigger | CARD_PATTERNS_CREATURES_TRIGGERED.md |
| lose a keyword and prevent opponents from gaining it | CARD_PATTERNS_PERMANENTS_STATIC.md and EFFECTS_QUICK_REFERENCE.md |
| first matching spell cast each turn costs less | CARD_PATTERNS_PERMANENTS_STATIC.md |
| random greatest-mana-value creature card in hand perpetually costs less | `f/FuelTankFeaster.java` + `PerpetualReduceRandomGreatestManaValueCreatureCardCostEffect` |
| creature card in your graveyard without unearth perpetually gains unearth; max speed makes first unearth free | `h/HighwayReaver.java` + `PerpetuallyGrantUnearthToTargetCreatureCardEffect` + `MaxSpeedFreeFirstUnearthEffect` |
| enchantment spell and Room unlock cost reduction | CARD_PATTERNS_PERMANENTS_STATIC.md |
| ETB, enters the battlefield | CARD_PATTERNS_CREATURES_ETB.md |
| face-down top-library exile plus two-player hidden 1/2/3 match ability | `e/ExpertLevelSafe.java` + `ExpertLevelSafeEffect` |
| foretell, enters with counters based on turns since foretell | `l/LupineHarbingers.java` + `EnterWithCountersEffect(PLUS_ONE_PLUS_ONE, new TurnsBegunSinceForetell())` |
| self-damage trigger exiles the source face down and makes it foretold | `t/TheForetoldSoldier.java` + `ExileSelfAndBecomeForetoldEffect` |
| ETB secretly choose an opponent permanent, then opponent sacrifices another and the chosen permanent | `TargetPlayerChoosesCreatureOrPlaneswalkerThenSacrificesChosenPermanentEffect` + CARD_PATTERNS_CREATURES_ETB.md |
| airbend, exile target nonland permanent for a {2} cast | CARD_PATTERNS_CREATURES_ETB.md |
| airbend all other creatures, opponents can't cast from outside hand | CARD_PATTERNS_LANDS_SPELLS.md |
| kicker, alternate casting cost | CARD_PATTERNS_CREATURES_ETB.md |
| Squad, repeatable additional cost, token copies, attacking tokens | CARD_PATTERNS_PERMANENTS_STATIC.md |
| buyback, return spell to hand | CARD_PATTERNS_LANDS_SPELLS.md |
| attack trigger, death trigger, upkeep trigger | CARD_PATTERNS_CREATURES_TRIGGERED.md |
| attack trigger, target controller may sacrifice or take damage | `s/StarAthlete.java` + `TargetPermanentControllerMaySacrificeOrDamageEffect` |
| attack trigger, memory counter, copy exiled creature cards | CARD_PATTERNS_CREATURES_TRIGGERED.md and EFFECTS_QUICK_REFERENCE.md |
| controller end-step trigger, memory counter, copy creature card in exile | `t/TheAnimus.java` and EFFECTS_QUICK_REFERENCE.md |
| combat damage mill + energy, brain counter, copy activated abilities from marked exiled cards | `r/RexCyberHound.java` and EFFECTS_QUICK_REFERENCE.md |
| clone copy with an added subtype and a same-name global combat-damage trigger | `p/PiratedCopy.java` + `CopyPermanentOnEnterEffect` + `ON_CREATURE_WITH_SAME_NAME_COMBAT_DAMAGE_TO_PLAYER` |
| combat damage → untap creatures + additional combat + repeat-player attack restriction | `p/PortRazer.java` |
| cast trigger, takeover counter, enter as a copy of a marked exiled creature card | `t/TheMasterFormedAnew.java` and EFFECTS_QUICK_REFERENCE.md |
| two target creatures get counters/keywords and only those creatures may attack in an added combat | `l/LastNightTogether.java` + target-bound untap/counter/keyword effects + `AdditionalCombatMainPhaseEffect(..., onlyTargetCreaturesCanAttack)` |
| combat damage modal, goad damaged player's creature, exile top card and cast with any-color mana | CARD_PATTERNS_CREATURES_TRIGGERED.md |
| ETB goads up to one creature per opponent and adds their total power as +1/+1 counters | `h/HavocEater.java` + `targetUpTo(PlayersInGame - 1, creatureAnOpponentControls)` + `AT_MOST_ONE_PER_CONTROLLER` + `TotalPowerOfTargetGroup` |
| +1/+1 counter placement trigger | CARD_PATTERNS_CREATURES_TRIGGERED.md |
| ally permanent death trigger snapshots total counters and optionally places +1/+1 counters on a creature | `y/YunaGrandSummoner.java` + `PutPlusOnePlusOneCountersOnTargetForEachDyingSourceCounterEffect` |
| counters placed on a creature you don't control | `ON_YOU_PUT_COUNTERS_ON_CREATURE_YOU_DONT_CONTROL` plus `TapPermanentsEffect(TRIGGERING)`, `GoadTriggeringCreatureUntilNextTurnEffect`, and `GrantKeywordEffect(TRAMPLE, TRIGGERING_PERMANENT, UNTIL_YOUR_NEXT_TURN)`; see `k/KrosDefenseContractor.java` |
| counter placement followed by goad of exactly the affected creatures, including an overload branch | `PutCountersOnTargetPermanentThenReflexiveEffect` with `GoadTriggeringCreatureUntilNextTurnEffect` for the targeted branch; `PutCounterOnEachMatchingPermanentThenGoadEffect` for the overloaded branch; see `s/SpectacularShowdown.java` |
| ward-like spell/ability counter plus target opponent copies a copied spell | `p/ParnesseTheSubtleBrush.java` |
| beginning-of-combat random counter trigger | CARD_PATTERNS_CREATURES_TRIGGERED.md |
| attack-triggered uniformly random choice among several effects | `c/CultOfSkaro.java` + `RandomChoiceEffect` |
| beginning-of-combat random-opponent attack trigger | CARD_PATTERNS_CREATURES_TRIGGERED.md |
| face-down permanent turns face up | CARD_PATTERNS_CREATURES_TRIGGERED.md |
| enter/turn face up power-scaled boost with negamorph | `f/FlavorDisaster.java` |
| end-step may manifest plus exile a face-down permanent and play that exact card | `p/PrimordialMist.java` + `AllowPlayExiledCostCardThisTurnEffect` |
| suspect a creature / clear suspected creatures | `SuspectEffect(GrantScope.TARGET)` + `UnsuspectAllCreaturesEffect`; for optional non-targeted selection use `MayEffect(SuspectChosenOtherCreatureEffect())` |
| combat damage trigger, block trigger | CARD_PATTERNS_CREATURES_TRIGGERED.md |
| combat damage → damaged player chooses a nonland permanent controlled by one of the source controller's opponents to destroy | `b/BladegriffPrototype.java` + `ON_COMBAT_DAMAGE_TO_PLAYER DestroyPermanentChosenByDamagedPlayerAmongOpponentsEffect` |
| combat damage to an opponent, same damage to each other opponent | CARD_PATTERNS_CREATURES_TRIGGERED.md |
| cast-trigger temporary creature theft plus enter tapped/stun counters and combat-damage-to-owner untap/draw | `t/TheBeastDeathlessPrince.java` + existing control, keyword, untap, counter, and draw effects |
| graveyard trigger, graveyard ability | CARD_PATTERNS_CREATURES_TRIGGERED.md |
| spell cast trigger, opponent spell | CARD_PATTERNS_CREATURES_TRIGGERED.md |
| cast trigger reveals each player's top card and sets entry counters | CARD_PATTERNS_CREATURES_TRIGGERED.md |
| first spell each turn, random opponent damage | CARD_PATTERNS_CREATURES_TRIGGERED.md |
| secret council vote, draw for one vote and damage a random opponent for the other | `t/TruthOrConsequences.java` + `TruthOrConsequencesEffect` |
| exact-1-damage source trigger reflected to each matching permanent or player | `g/GhyrsonStarnKelermorph.java` + `GhyrsonStarnKelermorphEffect` |
| beginning-of-combat random opponent attack requirement | `r/RuhanOfTheFomori.java` |
| attack-triggered left/right pile evasion | `r/RagingRiver.java` + `RagingRiverEffectHandler` |
| each opponent separates creatures into piles and controller chooses sacrifice pile | `MakeAnExampleEffect` + `MakeAnExampleEffectHandler` |
| global spell-cast exile/copy trigger | CARD_PATTERNS_PERMANENTS_ARTIFACTS.md |
| each player chooses a color, then selectively exile colored permanents by controller | `EachPlayerChoosesColorThenExileOtherPermanentsEffect` |
| first instant, sorcery, or subtype spell each turn — exile the triggering spell, dig to a nonland, damage by mana-value difference, and offer a free cast | CARD_PATTERNS_CREATURES_TRIGGERED.md |
| combat damage → random own-graveyard instant/sorcery, free cast at next upkeep | CARD_PATTERNS_CREATURES_TRIGGERED.md |
| random player chooses a graveyard instant/sorcery, then a free copy cast | `w/WildfireDevils.java` + `RandomPlayerExilesInstantOrSorceryAndMayCastCopyEffect` |
| chosen creature type, copy each matching creature you control, temporary hasty copies | `CreateTokenCopyOfEachCreatureOfChosenTypeEffect` + `CreateTokenCopyOfTargetPermanentEffect(true, true)` |
| demonstrate a spell copy for you and one chosen opponent | `MayEffect(new DemonstrateEffect(), "Copy [spell name]?")` in `ON_SELF_CAST` + the spell's normal effects |
| creature spells you cast have demonstrate | `STATIC GrantSpellCastingAbilityToSpellsEffect(Keyword.DEMONSTRATE, new CardTypePredicate(CardType.CREATURE))` | `ON_SELF_CAST` grant is materialized by the spell-cast trigger collector; Silverquill Lecturer (M3C 44) |
| demonstrate + destroy an opposing artifact/creature + its controller's immediate free cast from an exile-until-nonland dig | `MayEffect(new DemonstrateEffect())` + `DestroyTargetPermanentThenEffect(..., ThenEffectRecipient.TARGET_CONTROLLER, requiresDestruction=true)` + `ExileTopUntilNonlandMayCastWithoutPayingManaCostEffect` |
| each opponent digs to a nonland, then the controller may free-cast any number of those spells | `EachPlayerExilesTopUntilNonlandAndMayCastSpellsEffect(Integer.MAX_VALUE, false, LibraryScope.EACH_OPPONENT)` |
| chosen creature type, reveal until matching creature count, put matches onto battlefield | `RevealUntilChosenCreatureTypeCountToBattlefieldEffect` |
| encore, graveyard self-exile and attacking token copies for each opponent | `ExileSelfFromGraveyardCost` + `CreateTokenCopiesOfSourceAttackingOpponentsEffect` in a sorcery-speed graveyard ability |
| destroy target creature, then create two half-sized token copies | `DestroyTargetCreatureAndCreateTokenCopiesEffect` |
| cast-time X doubling, copy X spells or abilities | CARD_PATTERNS_PERMANENTS_STATIC.md |
| hand exile + token copy | CARD_PATTERNS_PERMANENTS_ARTIFACTS.md |
| token enters, target another player to copy it, once-per-turn conditional draw | `l/LucyMacLeanPositivelyArmed.java` + `CreateTokenCopyOfEnteringTokenForTargetPlayerEffect` |
| first token creation each turn may copy another creature | `e/EsixFractalBloom.java` + `EsixFractalBloomEffect` + `CreateTokenCopyOfChosenCreatureEffect` |
| encore, graveyard ability creates hasty copies attacking each opponent | `i/ImpulsivePilferer.java` + `EncoreEffect` |
| static encore grant to creature cards matching any of several subtypes | `GrantEncoreToCreatureCardsOfSubtypesEffect` + a sorcery-speed graveyard ability |
| landfall, land enters trigger | CARD_PATTERNS_CREATURES_TRIGGERED.md |
| each opponent may investigate, opponent choice plus controller Clues | CARD_PATTERNS_CREATURES_TRIGGERED.md |
| each player may put counters on a creature, and players who do cannot attack the trigger controller until their next turn | `o/OrzhovAdvokist.java` + `EachPlayerMayPutCountersOnCreatureEffect` |
| each opponent chooses one of three modes, controller and chosen opponent receive the matching reward | `m/MasterOfCeremonies.java` + `EachOpponentChoosesMasterOfCeremoniesEffect` |
| lord, anthem, static boost | CARD_PATTERNS_PERMANENTS_STATIC.md |
| commander-only anthem, opponent attacks with two creatures | CARD_PATTERNS_PERMANENTS_STATIC.md and CARD_PATTERNS_CREATURES_TRIGGERED.md |
| damage prevention into counters | CARD_PATTERNS_PERMANENTS_STATIC.md |
| damage prevention into tokens | `i/Inkshield.java` — `PreventAllCombatDamageToControllerAndCreateTokensEffect(CreateTokenEffect token)` |
| aura, enchant creature, pacifism | CARD_PATTERNS_PERMANENTS_STATIC.md |
| curse, enchant player | CARD_PATTERNS_PERMANENTS_STATIC.md |
| metalcraft, morbid, conditional | CARD_PATTERNS_PERMANENTS_STATIC.md |
| quest counter, opponent end step trigger, life-loss condition | CARD_PATTERNS_PERMANENTS_STATIC.md |
| attack-count-gated quest counter, sacrifice draw per quest counter | CARD_PATTERNS_PERMANENTS_ARTIFACTS.md |
| pay-life trigger, counters from life paid, counter-removal ability | CARD_PATTERNS_PERMANENTS_STATIC.md |
| protection from modified creatures | CARD_PATTERNS_PERMANENTS_STATIC.md |
| as-enters card-type choice, controller and own creatures gain protection from chosen card type | `ChooseCardTypeOnEnterEffect` + `GrantProtectionFromChosenCardTypeToControllerAndOwnCreaturesEffect` |
| as-enters card-type choice, players can't cast the chosen type | `ChooseCardTypeOnEnterEffect` + `PlayersCantCastSpellsMatchingPredicateEffect(new CardHasSourceChosenCardTypePredicate())` |
| postcombat main may-pay-life draw based on opponents dealt combat damage | CARD_PATTERNS_CREATURES_TRIGGERED.md |
| artifact, charge counter, spellbomb | CARD_PATTERNS_PERMANENTS_ARTIFACTS.md |
| create a token this turn, conditional draw artifact ability | `i/IdolOfOblivion.java` |
| vehicle, crew | CARD_PATTERNS_PERMANENTS_ARTIFACTS.md |
| equipment, equip, living weapon | CARD_PATTERNS_PERMANENTS_ARTIFACTS.md |
| Equipment attack trigger with a conditional perpetual boost based on a shared creature name or graveyard creature card | `m/MaceOfDisruption.java` + `PerpetuallyBoostEquippedCreatureIfNameSharedEffect` |
| activated ability, tap ability, sacrifice ability | CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md |
| X-paid face-down creature cast from hand with damage/tap turn-up clause | CARD_PATTERNS_PERMANENTS_ARTIFACTS.md |
| base power from target creature, indefinitely | CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md |
| mana ability, mana dork | CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md |
| commander-identity mana plus shared-creature-type spell trigger | CARD_PATTERNS_LANDS_SPELLS.md |
| planeswalker, loyalty | CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md |
| Aura turns a creature into a planeswalker whose toughness is its loyalty | CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md |
| saga, chapter, lore counter | CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md |
| Saga searches until a legendary card, then grants source-duration normal-cost play permission | `t/TheDayOfTheDoctor.java` + `ExileUntilCardPredicateMayPlayWhileSourceControlledEffect` |
| upkeep life loss, exile top card, and indefinite play permission | `r/RassilonTheWarPresident.java` + `ExileTopCardMayPlayWhileExiledEffect` |
| Saga chooses matching creatures, then optionally exiles the rest and deals damage | `t/TheDayOfTheDoctor.java` + `ChooseUpToNMatchingCreaturesThenMayExileRestEffect` |
| Saga reveal of up to five nonland hand cards, grouped by mana value into Treasure tokens | `v/Vault21HouseGambit.java` + `RevealUpToFiveNonlandCardsFromHandThenCreateTreasureTokensEffect` |
| template, copy-paste, skeleton | CARD_COPY_PASTE_TEMPLATES.md |

## Canonical test reference per pattern

When implementing a card, use these as the **best** test file to read for each common pattern:

| Pattern | Best test reference | Why |
|---------|-------------------|-----|
| Activated ability requiring mana from a multicolored-capable source | `e/ExperimentFive.java` / `ExperimentFiveTest.java` | `PayMulticoloredSourceManaCost` consumes source-capability-tagged mana before the ability's ordinary generic mana cost |
| Aura with static boost (+X/+Y or -X/-Y) | `SensoryDeprivationTest.java` | Covers casting, resolution, stat check, removal, fizzle, targeting |
| Aura lockdown (can't attack/block) | `PacifismTest.java` | Covers combat restriction + removal |
| Targeted temporary static combat tax | `WhipgrassEntanglerTest.java` | Covers dynamic attack/block payment and cleanup duration |
| Simple burn spell | `ShockTest.java` | Covers creature + player targeting + fizzle |
| Non-targeted pump | `ChargeTest.java` | Covers boost + opponent unaffected + cleanup reset |
| ETB creature (non-targeted) | `AngelOfMercyTest.java` | Covers ETB trigger resolution |
| ETB creature (targeted) | `BriarpackAlphaTest.java` | Covers targeted ETB + fizzle + flash |
| Counterspell | `CancelTest.java` | Covers counter + graveyard |
| Target creature exile with suspend counters | `s/SuspendTest.java` | Covers exile, suspend countdown, free cast, and creature-only targeting |
| ETB reveal until nonland mana value 3 or greater, exile it with suspend, and bottom the rest randomly | `s/SibyllineSoothsayer.java` | `RevealUntilNonlandWithManaValueAtLeastAndSuspendEffect(3, 3)` |
| Draw, then controller and target opponent may exile nonland hand cards with mana-value time counters | `t/TheWeddingOfRiverSongTest.java` | Covers draw order, target-player may choice, nonland hand filtering, and dynamic suspend counters |
| Exile top cards, play them, and put time counters on exiled cards with suspend | `e/EcstaticBeautyTest.java` | Covers scoped suspend counters, play permissions, and the suspend-from-hand ability |
| combat damage chooses one suspended card you own and removes that many time counters | `a/AmyPond.java` + `ChooseSuspendedCardAndRemoveTimeCountersEffect` |
| combat damage may exile any hand card with mana-value time counters and suspend it | `t/TheEleventhDoctor.java` + `ExileCardFromHandWithManaValueTimeCountersEffect` |
| upkeep time counter, controlled-creature untap and source-linked phase-out, then sorcery-speed nonland sweep by source counters and self-sacrifice | `t/TheMoment.java` + `PermanentManaValueAtMostSourceCountersPredicate` + `PhaseOutTargetCreatureUntilSourceLeavesEffect` |
| Time-counter placement trigger plus optional dynamic attack boost | `k/KateStewartTest.java` | Covers controlled-permanent scope, entry counters, attacking-only boost, and mana payment |
| Vanishing enchantment whose time-counter removal scries, gains life, and exiles for an extra turn on the last counter | `r/RegenerationsRestored.java` | Dedicated permanent time-counter-removal trigger plus existing vanishing/exile/extra-turn effects |
| Draw spell | `CounselOfTheSoratamiTest.java` | Covers draw count + graveyard |
| Destroy spell | `TerrorTest.java` | Covers destroy + filter + fizzle |
| Equipment | `LeoninScimitarTest.java` | Covers equip + boost + unequip |
| Equipment with per-player life-loss counter trigger | `r/ReapersScythe.java` | `CONTROLLER_END_STEP_TRIGGERED` puts one soul counter per distinct player who lost life; `AttachedBoostEffect` scales the equipped creature and `GrantSubtypeEffect` adds Assassin |
| Lord/anthem | `GloriousAnthemTest.java` | Covers static boost + removal |
| Vanilla creature | (no test needed) | Empty body, no engine logic |
| Tapped artifact token with its own targeted ETB and produced-mana trigger | `r/RoxanneStarfallSavant.java` | Use the full `CreateTokenEffect` constructor for non-creature token state, put the damage ability in `ON_ENTER_BATTLEFIELD`, and put the dynamic mana rider in `ON_SELF_TAPPED_FOR_MANA` |
| ETB creates an Equipment token and attaches it to the source | `u/USAgentJohnWalker.java` | Use `CreateTokenAndAttachToSourceEffect(CreateTokenEffect.ofArtifactToken(...).withTokenEffects(Map.of(STATIC, new StaticBoostEffect(...))))`; put the token's `EquipActivatedAbility` in its token ability list |
| Equipment swap ability | `a/ArcanumThings.java` | Add the equipped-creature static effect, `EquipActivatedAbility`, and an activated ability with `EquipmentSwapEffect()` |

| activated ability tracks cards exiled as a cost for an opponent's choice | `c/CoinOfFate.java` / `c/CoinOfFateTest.java` | Mark the graveyard-exile cost as tracked, resolve the choice through a pending interaction, and resume the parked ability through `InputCompletionService` |

### Subgames

Shahrazad (ARN 10): `StartSubgameEffect` followed by the existing fractional life-loss effect with `SUBGAME_NON_WINNERS`. The creating spell stays suspended until its immediate child ends. Use `GameSession` for nesting; never copy the parent board into a child or restore an old library snapshot.
| fixed list of cards, choose one at random, and create a token copy | `CreateTokenCopyOfRandomCardEffect(List<Supplier<? extends Card>>)` | Who's That Praetor? (MB2 270/506) |
| fixed list of cards, choose one at random, create a copy, and offer it for a free cast | `CreateRandomCardCopyAndMayCastEffect(List<Supplier<? extends Card>>)` | Jund 'Em Out (MB2 357/596) |
| combat-damage trigger that randomly conjures one of several named cards into hand and reveals it | `p/PinchyMcStingbutt.java` + `ConjureRandomCardFromSpellbookToHandEffect` |
| Perpetual ETB ability granted to a card in hand | `PullOfTheMistMoonTest.java` | Covers the hand-card choice and the stored ability triggering when a later copy enters |
| Kicked bounce that conjures a castable duplicate into hand | `VesuvanMistTest.java` | Covers nontoken/nonland targeting, last-known-information copying, persistent token-card handling, and card-local any-color mana permission |
| Kicked ETB returns your graveyard card and conjures an opponent-graveyard duplicate | `NantukoSlicerTest.java` | Covers independently targeted graveyard groups, kicked-only opponent targeting, and perpetual any-color casting permission on the conjured card |
| Multi-target independent may exile followed by survivor-count search | `d/DisorientingChoiceTest.java` | Covers per-controller keep/exile choices, no-search when all targets leave, tapped land search, and artifact/enchantment opponent targeting |
| each creature you control explores, then explores again | `STATIC DoubleExploreReplacementEffect()`; `ExploreEffectHandler` expands each controlled creature's explore into two replacement-exempt explores |
| Exile each player's top card and let the spell controller play them through their next turn | `l/LidlessGaze.java` + `ExileTopCardOfEachPlayersLibraryMayPlayUntilNextTurnEffect` |
| chosen nonbasic land type, make matching lands you control hasty copies of a target creature you control | `EachControlledLandOfChosenNonbasicTypeBecomesCopyOfTargetCreatureUntilEndOfTurnEffect` + `FlashbackCast` |
| return a land as an activated cost, then draw and conditionally discard by its nonbasic type | `CARD_PATTERNS_LANDS_SPELLS.md` |
| Targeted creature exile and controller cast permission through their next turn | `h/HurlThroughHell.java` + `ExileTargetPermanentAndGrantControllerCastPermissionUntilNextTurnEffect(true)` | Covers creature targeting, controller-relative expiry, and any-color mana |
