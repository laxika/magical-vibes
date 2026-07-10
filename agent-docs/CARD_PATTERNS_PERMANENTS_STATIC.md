# Card Patterns: Static Permanents & Auras

All paths relative to `cards/`.

## Static permanents

| Pattern | Reference | Notes |
|---------|-----------|-------|
| Subtype lord (all) | `g/GoblinKing.java` | STATIC StaticBoostEffect with PermanentHasAnySubtypePredicate filter, ALL_CREATURES scope |
| Subtype lord (own) + keyword | `k/KnightExemplar.java` | STATIC StaticBoostEffect(1, 1, Set.of(INDESTRUCTIBLE), OWN_CREATURES, PermanentHasAnySubtypePredicate) — +1/+1 and indestructible to other Knights you control |
| Anthem (all own) | `g/GloriousAnthem.java` | STATIC StaticBoostEffect with OWN_CREATURES scope, no filter |
| Multi-subtype lord + transform lock | `i/Immerwolf.java` | STATIC StaticBoostEffect(1, 1, OWN_CREATURES, PermanentHasAnySubtypePredicate({WOLF, WEREWOLF})) + STATIC PreventTransformEffect(PermanentAllOfPredicate(WEREWOLF, NOT HUMAN)) — +1/+1 to other Wolves/Werewolves you control and "Non-Human Werewolves you control can't transform" |
| Supertype lord (own) | `a/ArvadTheCursed.java` | STATIC StaticBoostEffect(2, 2, OWN_CREATURES, PermanentHasSupertypePredicate(LEGENDARY)) — +2/+2 to other legendary creatures you control |
| Color boost/debuff | `a/AscendantEvincar.java` | STATIC StaticBoostEffect with PermanentColorInPredicate / PermanentNotPredicate filter |
| Own boost + opponent debuff | `e/EleshNornGrandCenobite.java` | STATIC StaticBoostEffect(2, 2, OWN_CREATURES) + StaticBoostEffect(-2, -2, OPPONENT_CREATURES) |
| Color keyword lord | `b/BellowingTanglewurm.java` | STATIC GrantKeywordEffect with PermanentColorInPredicate filter, OWN_CREATURES scope |
| Keyword lord + spell trigger | `h/HandOfThePraetors.java` | STATIC StaticBoostEffect with PermanentHasKeywordPredicate(INFECT) filter, OWN_CREATURES scope + ON_CONTROLLER_CASTS_SPELL GivePoisonCountersEffect(1, PoisonRecipient.TARGET_PLAYER, CardAllOfPredicate(CREATURE, INFECT)) |
| Attachment self-buff | `c/ChampionOfTheFlame.java` | STATIC BoostSelfEffect(Scaled(AttachmentsOnSource(true, true), 2), same) — +2/+2 for each Aura and Equipment attached. Equipment-only: AttachmentsOnSource(false, true) (Goblin Gaveleer) |
| Tribal combat trigger (subtype counter lord) | `r/RakishHeir.java` | ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER AllyCombatDamageTriggerEffect(PermanentHasSubtypePredicate(VAMPIRE), PutCountersOnSourceEffect(1,1,1), bindSourceToDealer=true) — when a Vampire you control deals combat damage to a player, put +1/+1 counter on it |
| Choose subtype + grant to own | `x/Xenograft.java` | ON_ENTER_BATTLEFIELD ChooseSubtypeOnEnterEffect + STATIC GrantChosenSubtypeToOwnCreaturesEffect |
| Choose subtype + lord boost + cast draw | `v/VanquishersBanner.java` | ON_ENTER_BATTLEFIELD ChooseSubtypeOnEnterEffect + STATIC BoostCreaturesOfChosenSubtypeEffect(1,1) + ON_CONTROLLER_CASTS_SPELL ChosenSubtypeSpellCastTriggerEffect(DrawCardEffect) — choose type on enter, +1/+1 to own creatures of that type, draw on casting creature of that type |
| Shared-type pump | `c/CoatOfArms.java` | STATIC BoostBySharedCreatureTypeEffect |
| Can't block | `s/SpinelessThug.java` | STATIC CantBlockEffect |
| Must attack | `b/BloodrockCyclops.java` | STATIC MustAttackEffect |
| Evasion (blocked only by) | `e/ElvenRiders.java` | STATIC CanBeBlockedOnlyByFilterEffect |
| Block limit | `s/StalkingTiger.java` | STATIC CanBeBlockedByAtMostNCreaturesEffect |
| Unblockable | `p/PhantomWarrior.java` | STATIC CantBeBlockedEffect |
| Conditional unblockable | `s/ScrapdiverSerpent.java` | STATIC CantBeBlockedIfDefenderControlsMatchingPermanentEffect |
| Unblockable while attacking alone | `d/DreamProwler.java` | STATIC CantBeBlockedIfAttackingAloneEffect |
| Grant unblockable to own creatures | `t/TetsukoUmezawaFugitive.java` | STATIC GrantEffectEffect(CantBeBlockedEffect, ALL_OWN_CREATURES, PermanentAnyOfPredicate(power/toughness filter)) |
| Attack restriction (defender controls) | `s/SeaMonster.java` | STATIC CantAttackUnlessEffect(new DefendingPlayerControlsPermanent(predicate), desc) — can't attack unless defending player controls a matching permanent |
| Attack restriction (controller controls) | `d/DesperateCastaways.java` | STATIC CantAttackUnlessEffect(new ControlsPermanentCount(1, predicate), desc) — can't attack unless controller controls a matching permanent |
| Attack restriction (battlefield count) | `h/HarborSerpent.java` | STATIC CantAttackUnlessEffect(new AnyPlayerControlsPermanentCount(5, predicate), desc) — can't attack unless N+ matching permanents across all battlefields |
| Attack restriction (defender poisoned) | `c/ChainedThroatseeker.java` | STATIC CantAttackUnlessEffect(new DefendingPlayerPoisoned(), desc) — can't attack unless defending player is poisoned |
| Attack restriction (opponent damage) | `b/BloodcrazedGoblin.java` | STATIC CantAttackUnlessEffect(new OpponentDealtDamageThisTurn(), desc) — can't attack unless an opponent was dealt damage this turn |
| Block restriction | `c/CloudElemental.java` | STATIC CanBlockOnlyIfAttackerMatchesPredicateEffect |
| Protection from colors | `p/PaladinEnVec.java` | STATIC ProtectionFromColorsEffect |
| Grant protection from colors to greatest-MV creatures | `f/FavorOfTheMighty.java` | STATIC GrantEffectEffect(ProtectionFromColorsEffect(all 5 colors), ALL_CREATURES, PermanentHasGreatestManaValueAmongAllCreaturesPredicate) — granted protection is read by GameQueryService.hasProtectionFrom via the static bonus's grantedEffects |
| Prevent all damage to self | `c/ChoMannoRevolutionary.java` | STATIC PreventAllDamageEffect |
| Can't lose game | `p/PlatinumAngel.java` | STATIC CantLoseGameEffect |
| Can't lose from life + damage as infect | `p/PhyrexianUnlife.java` | STATIC CantLoseGameFromLifeEffect + DamageDealtAsInfectBelowZeroLifeEffect |
| Damage can't reduce life below 1 (if control creature) | `w/Worship.java` | STATIC DamageCantReduceLifeBelowOneEffect |
| Can't lose + life gain draw + life loss exile + LTB lose | `l/LichsMastery.java` | STATIC CantLoseGameEffect + GrantKeywordEffect(HEXPROOF, SELF), ON_CONTROLLER_GAINS_LIFE DrawCardsEqualToLifeGainedEffect, ON_CONTROLLER_LOSES_LIFE ExileForEachLifeLostEffect, ON_SELF_LEAVES_BATTLEFIELD ControllerLosesGameOnLeavesEffect |
| Controller shroud | `t/TrueBeliever.java` | STATIC GrantControllerShroudEffect |
| Can't cast type | `s/SteelGolem.java` | STATIC CantCastSpellTypeEffect |
| Limit spells (all players) | `r/RuleOfLaw.java` | STATIC LimitSpellsPerTurnEffect |
| Limit spells (enchanted player) | `c/CurseOfExhaustion.java` | STATIC LimitSpellsForEnchantedPlayerEffect |
| Tax attackers | `w/WindbornMuse.java` | STATIC RequirePaymentToAttackEffect |
| Tax opponent spells | `a/AuraOfSilence.java` | STATIC IncreaseOpponentCastCostEffect |
| Tax matching spells (all players) | `t/ThaliaGuardianOfThraben.java` | STATIC IncreaseSpellCostEffect(CardNotPredicate(CardTypePredicate(CREATURE)), 1) — symmetric, affects all players |
| Enters tapped | `r/RootMaze.java` | STATIC EnterPermanentsOfTypesTappedEffect |
| Opponent creatures enter tapped + haste lord | `u/UrabraskTheHidden.java` | STATIC GrantKeywordEffect(HASTE, OWN_CREATURES) + EnterPermanentsOfTypesTappedEffect(CREATURE, opponentsOnly=true) |
| P/T = lands | `m/MolimoMaroSorcerer.java` | STATIC SetPowerToughnessToAmountEffect(a, a) where a = PermanentCount(PermanentIsLandPredicate, CONTROLLER) |
| P/T = creatures | `s/ScionOfTheWild.java` | STATIC SetPowerToughnessToAmountEffect(a, a) where a = PermanentCount(PermanentIsCreaturePredicate, CONTROLLER) |
| P/T = subtype | `n/Nightmare.java` | STATIC SetPowerToughnessToAmountEffect(a, a) where a = PermanentCount(PermanentHasSubtypePredicate(SWAMP), CONTROLLER) |
| P/T = GY creatures | `m/Mortivore.java` | STATIC SetPowerToughnessToAmountEffect(a, a) where a = CardsInGraveyard(CardTypePredicate(CREATURE), ANY_PLAYER) |
| P/T = hand size + draw trigger | `p/PsychosisCrawler.java` | STATIC SetPowerToughnessToAmountEffect(a, a) where a = CardsInHand(CONTROLLER) + ON_CONTROLLER_DRAWS LoseLifeEffect(1, EACH_OPPONENT) |
| Self boost per lands + GY lands | `m/MultaniYavimayasAvatar.java` | STATIC BoostSelfEffect(PermanentCount(PermanentIsLandPredicate, CONTROLLER), same) + BoostSelfEffect(CardsInGraveyard(CardTypePredicate(LAND), CONTROLLER), same) — +1/+1 per land you control and per land card in your graveyard |
| Gain GY creature abilities | `n/NecroticOoze.java` | STATIC GainActivatedAbilitiesOfCreatureCardsInAllGraveyardsEffect — selfOnly, gains all activated abilities of all creature cards in all graveyards |
| +1/+1 per same name | `r/RelentlessRats.java` | STATIC BoostByOtherCreaturesWithSameNameEffect |
| +1/+0 per other subtype you control | `r/RatColony.java` | STATIC BoostSelfEffect(PermanentCount(PermanentHasSubtypePredicate(RAT), CONTROLLER, excludeSource=true), Fixed(0)) |
| Cost reduction if opponent has more creatures | `a/AvatarOfMight.java` | STATIC ConditionalEffect(OpponentControlsMoreCreatures(4), ReduceOwnCastCostEffect(Fixed(6))) — costs {6} less if an opponent controls 4+ more creatures than you |
| Subtype cost reduction | `d/DanithaCapashenParagon.java` | STATIC ReduceCastCostForMatchingSpellsEffect(CardAnyOfPredicate(CardSubtypePredicate(AURA), CardSubtypePredicate(EQUIPMENT)), 1, SELF) — from battlefield permanent |
| Cost reduction per creature card in graveyard | `g/Ghoultree.java` | STATIC ReduceOwnCastCostEffect(CardsInGraveyard(CardTypePredicate(CREATURE), CONTROLLER)) — this spell costs {1} less per creature card in your graveyard |
| Cost reduction per creature on battlefield | `b/BlasphemousAct.java` | STATIC ReduceOwnCastCostEffect(PermanentCount(PermanentIsCreaturePredicate, ANY_PLAYER)) — this spell costs {1} less for each creature on the battlefield |
| Creature mana only | `m/MyrSuperion.java` | setRequiresCreatureMana(true) — can only be cast with mana produced by creatures |
| No max hand size | `s/Spellbook.java` | STATIC NoMaximumHandSizeEffect |
| Toughness as combat damage (controller) | `b/BelligerentBrontodon.java` | STATIC AssignCombatDamageWithToughnessEffect(ALL_OWN_CREATURES) — all your creatures assign combat damage equal to toughness |
| Double damage (global) | `f/FurnaceOfRath.java` | STATIC DoubleDamageEffect |
| Double damage (controller's all sources) | `a/AngrathsMarauders.java` | STATIC DoubleControllerDamageEffect(null, true) — doubles all damage from sources you control (combat, spells, abilities) |
| Double damage (controller's spells by color) | `f/FireServant.java` | STATIC DoubleControllerDamageEffect(AllOf[TypeIn(INSTANT,SORCERY), ColorIn(RED)], false) — doubles only red instant/sorcery damage |
| Double damage to enchanted player (Curse) | `c/CurseOfBloodletting.java` | STATIC DoubleDamageToEnchantedPlayerEffect — enchant player curse; doubles all damage dealt to the enchanted player (spell/ability via DamageResolutionService.dealDamageToPlayer, combat via CombatDamageService.applyPlayerDamage), using GameQueryService.getEnchantedPlayerDamageMultiplier. Stacks multiplicatively with Furnace of Rath |
| Play lands from GY | `c/CrucibleOfWorlds.java` | STATIC PlayLandsFromGraveyardEffect |
| Draw replacement | `a/Abundance.java` | STATIC AbundanceDrawReplacementEffect |
| Grant flash to spell type | `s/ShimmerMyr.java` | STATIC GrantFlashToCardTypeEffect(ARTIFACT) — controller may cast artifact spells as though they had flash |
| Grant flash to all spells + leyline | `l/LeylineOfAnticipation.java` | ON_OPENING_HAND_REVEAL MayEffect(LeylineStartOnBattlefieldEffect) + STATIC GrantFlashToCardTypeEffect(null) — may start on battlefield from opening hand, grants flash to all spells |
| Metalcraft keyword | `a/AuriokEdgewright.java` | STATIC ConditionalEffect(new Metalcraft(), GrantKeywordEffect(DOUBLE_STRIKE, SELF)) |
| Metalcraft keyword + boost | `a/AuriokSunchaser.java` | STATIC ConditionalEffect(new Metalcraft(), GrantKeywordEffect) + ConditionalEffect(new Metalcraft(), StaticBoostEffect) |
| Metalcraft boost only | `c/CarapaceForger.java` | STATIC ConditionalEffect(new Metalcraft(), StaticBoostEffect(2, 2, SELF)) |
| Metalcraft boost + ignore defender | `s/SpireSerpent.java` | STATIC ConditionalEffect(new Metalcraft(), StaticBoostEffect) + ConditionalEffect(new Metalcraft(), CanAttackAsThoughNoDefenderEffect) |
| Metalcraft become creature | `r/RustedRelic.java` | STATIC ConditionalEffect(new Metalcraft(), AnimatePermanentsEffect(5, 5, [GOLEM], [])) — noncreature becomes creature with fixed P/T and subtypes (4-arg SELF/UEOT ctor) |
| Opponent-turn become creature | `w/WardenOfTheWall.java` | STATIC EntersTappedEffect + mana ability + ConditionalEffect(new NotControllerTurn(), AnimatePermanentsEffect(2, 3, [GARGOYLE], [FLYING])) — artifact animates on opponent's turn only |
| Combat-only self-animate until end of combat | `j/JadeStatue.java` | Activated ability `{2}` with `ActivationTimingRestriction.ONLY_DURING_COMBAT` + `AnimatePermanentsEffect(3, 6, [GOLEM], Set.of(), null, Set.of(), GrantScope.SELF, EffectDuration.UNTIL_END_OF_COMBAT)` — artifact becomes a 3/6 creature only during combat; reverts when combat ends |
| Metalcraft burn spell | `g/GalvanicBlast.java` | SPELL ConditionalReplacementEffect(new Metalcraft(), baseEffect, upgradedEffect)(DealDamageToAnyTargetEffect(2), DealDamageToAnyTargetEffect(4)) — picks base/upgrade at resolution |
| Morbid burn spell | `b/BrimstoneVolley.java` | SPELL ConditionalReplacementEffect(new Morbid(), baseEffect, upgradedEffect)(DealDamageToAnyTargetEffect(3), DealDamageToAnyTargetEffect(5)) — picks base/upgrade at resolution based on creature death this turn |
| Morbid land search | `c/CaravanVigil.java` | SPELL ConditionalReplacementEffect(new Morbid(), baseEffect, upgradedEffect)(SearchLibraryEffect(CardPredicateUtils.basicLand()), SearchLibraryEffect(basicLand filter, LibrarySearchDestination.BATTLEFIELD)) — search for basic land to hand, or to battlefield untapped if morbid |
| Morbid ETB counters | `f/FesterhideBoar.java` | ON_ENTER_BATTLEFIELD ConditionalEffect(new Morbid(), PutCountersOnSourceEffect(1, 1, 2)) — enters with +1/+1 counters if a creature died this turn |
| Metalcraft damage + can't block | `c/ConcussiveBolt.java` | SPELL DealDamageToPlayersEffect(4, DamageRecipient.TARGET_PLAYER) + ConditionalEffect(new Metalcraft(), new CantBlockThisTurnEffect(TapUntapScope.TARGET_PLAYERS_PERMANENTS)) — damage always, metalcraft adds mass can't-block on target player's creatures (rides the shared player target) |
| Life threshold boost + keyword | `s/SerraAscendant.java` | STATIC ConditionalEffect(new ControllerLifeAtLeast(30), StaticBoostEffect(5, 5, [FLYING], SELF)) — +5/+5 and flying as long as controller has 30+ life |
| Life threshold transform (activated) | `c/ChaliceOfLife.java` | ActivatedAbility(tap, [GainLifeEffect(1), ConditionalEffect(new ControllerLifeAtLeast(30), TransformSelfEffect())]) — gain 1 life then transform if 30+ life |
| Graveyard card threshold boost + keyword | `g/GhituLavarunner.java` | STATIC ConditionalEffect(new GraveyardCardThreshold(2, instant/sorcery filter), StaticBoostEffect(1, 0, [HASTE], SELF)) — +1/+0 and haste as long as 2+ instant/sorcery cards in controller's graveyard |
| Controls-subtype self-boost | `t/TeferisSentinel.java` | STATIC ConditionalEffect(new ControlsPermanent(PermanentHasSubtypePredicate(TEFERI)), StaticBoostEffect(4, 0, SELF)) — +4/+0 as long as you control a Teferi planeswalker |
| Controls-subtype self-boost + unblockable | `j/JacesSentinel.java` | STATIC ConditionalEffect(new ControlsPermanent(PermanentHasSubtypePredicate(JACE)), StaticBoostEffect(1, 0, SELF)) + ConditionalEffect(new ControlsPermanent(PermanentHasSubtypePredicate(JACE)), GrantEffectEffect(CantBeBlockedEffect, SELF)) — +1/+0 and can't be blocked as long as you control a Jace planeswalker |

## Auras

| Pattern | Reference | Notes |
|---------|-----------|-------|
| Lockdown (can't attack/block) | `p/Pacifism.java` | STATIC EnchantedCreatureCantAttackOrBlockEffect |
| Predicate-conditional aura | `b/BondsOfFaith.java` | STATIC EnchantedPermanentConditionalEffect(PermanentHasSubtypePredicate(HUMAN), StaticBoostEffect(2, 2, GrantScope.ENCHANTED_CREATURE), EnchantedCreatureCantAttackOrBlockEffect()) — composes existing effects, +2/+2 if Human, can't attack/block otherwise |
| Lockdown (can't attack) + self-bounce | `f/ForcedWorship.java` | STATIC EnchantedCreatureCantAttackEffect + activated ReturnToHandEffect.self() |
| Full lockdown (can't attack/block/activate) | `a/Arrest.java` | STATIC EnchantedCreatureCantAttackOrBlockEffect + EnchantedCreatureCantActivateAbilitiesEffect |
| P/T override + ability strip aura | `d/DeepFreeze.java` | STATIC SetBasePowerToughnessEffect(0, 4, ENCHANTED_CREATURE) + GrantKeywordEffect(DEFENDER, ENCHANTED_CREATURE) + LosesAllAbilitiesEffect(ENCHANTED_CREATURE) + GrantColorEffect(BLUE, ENCHANTED_CREATURE) + GrantSubtypeEffect(WALL, ENCHANTED_CREATURE) — sets base P/T, grants defender, strips all original abilities, adds color and type |
| Lockdown + self-destruct on target | `i/IceCage.java` | STATIC EnchantedCreatureCantAttackOrBlockEffect + EnchantedCreatureCantActivateAbilitiesEffect + ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY DestroySourcePermanentEffect |
| Doesn't untap | `d/Dehydration.java` | STATIC EnchantedCreatureDoesntUntapEffect |
| Static boost | `h/HolyStrength.java` | STATIC StaticBoostEffect(X, Y, GrantScope.ENCHANTED_CREATURE) |
| Boost + keyword | `s/SerrasEmbrace.java` | Boost + GrantKeywordEffect(ENCHANTED_CREATURE) |
| Keyword + combat damage prevention | `g/GhostlyPossession.java` | GrantKeywordEffect(FLYING) + PreventAllCombatDamageToAndByEnchantedCreatureEffect (non-combat damage still goes through) |
| Aura boost per controlled subtype | `b/BlanchwoodArmor.java` | STATIC AttachedBoostEffect(PermanentCount(PermanentHasSubtypePredicate(FOREST), CONTROLLER), same, GrantScope.ENCHANTED_CREATURE) — enchanted creature gets +1/+1 per Forest you control |
| Control enchanted | `p/Persuasion.java` | STATIC ControlEnchantedCreatureEffect |
| Grant activated ability | `a/ArcaneTeachings.java` | GrantActivatedAbilityEffect with GrantScope.ENCHANTED_CREATURE |
| Redirect damage to creature | `p/Pariah.java` | STATIC RedirectPlayerDamageToEnchantedCreatureEffect |
| Prevent X + redirect to player | `v/VengefulArchon.java` | Activated {X}: PreventXDamageToControllerAndRedirectToTargetPlayerEffect — prevent next X damage to you, source deals that much to target player |
| Enchanted land mana | `o/Overgrowth.java` | ON_ANY_PLAYER_TAPS_LAND AddManaOnEnchantedLandTapEffect |
| Enchanted land grant mana ability + ETB counter | `n/NewHorizons.java` | STATIC GrantActivatedAbilityEffect(AwardAnyColorManaEffect(2), ENCHANTED_PERMANENT) + ON_ENTER_BATTLEFIELD PutCounterOnTargetPermanentEffect(PLUS_ONE_PLUS_ONE, 1) — multi-target aura: first target is land (aura attachment), second is creature (ETB +1/+1 counter). Uses GrantScope.ENCHANTED_PERMANENT for non-creature aura targets |
| Enchanted land becomes basic type | `e/EvilPresence.java` | STATIC EnchantedPermanentBecomesTypeEffect(SWAMP) — land loses all land types/abilities, becomes the new basic land type |
| Aura with tap enchanted creature ability | `b/BurdenOfGuilt.java` | Activated TapPermanentsEffect(TapUntapScope.ENCHANTED) — aura's own ability pays {1} to tap the enchanted creature (no targeting, finds creature via attachedTo) |
| Aura + self-bounce ability | `s/ShimmeringWings.java` | STATIC keyword + activated ReturnToHandEffect.self() |
| Grant keyword + upkeep counter + death return | `g/GlisteningOil.java` | STATIC GrantKeywordEffect(INFECT, ENCHANTED_CREATURE) + UPKEEP_TRIGGERED PutCounterOnEnchantedCreatureEffect(CounterType.MINUS_ONE_MINUS_ONE) + ON_DEATH ReturnCardFromGraveyardEffect.builder().destination(HAND).filter(CardIsSelfPredicate).build() |
| Upkeep +1/+1 counter + sacrifice on combat | `p/PrimalCocoon.java` | UPKEEP_TRIGGERED PutCounterOnEnchantedCreatureEffect(CounterType.PLUS_ONE_PLUS_ONE) + ON_ATTACK SacrificeSelfEffect + ON_BLOCK SacrificeSelfEffect |
| Doesn't untap + enchanted controller upkeep life loss | `n/NumbingDose.java` | STATIC DoesntUntapEffect.enchanted() + ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED EnchantedCreatureControllerLosesLifeEffect(1) — enchants artifact or creature, uses PermanentAnyOfPredicate target filter |
| Enchanted permanent death trigger (gain life) | `v/ViridianHarvest.java` | ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD GainLifeEffect(6) — enchants artifact, aura controller gains life when enchanted permanent is put into graveyard |
| Enchanted permanent LTB trigger (conditional draw) | `c/CuratorsWard.java` | STATIC GrantKeywordEffect(HEXPROOF, ENCHANTED_CREATURE) + ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD EnchantedPermanentLeavesConditionalEffect(CardIsHistoricPredicate, DrawCardEffect(2)) — enchants any permanent, grants hexproof, draws 2 when enchanted permanent leaves battlefield if historic |
| Enchanted creature dealt damage trigger | `s/SpitefulShadows.java` | ON_ENCHANTED_CREATURE_DEALT_DAMAGE EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect — enchanted creature deals damage equal to amount dealt to its controller |
| Any creature dealt damage → destroy it | `d/DeathPitsOfRath.java` | ON_ANY_CREATURE_DEALT_DAMAGE DestroyTargetPermanentEffect(true) — global enchantment; fires once per damaged creature (combat + noncombat), queued entry auto-targets the damaged creature and destroys it, no regeneration |
| Curse (enchant player + static debuff) | `c/CurseOfDeathsHold.java` | STATIC StaticBoostEffect(-1, -1, GrantScope.ENCHANTED_PLAYER_CREATURES) — enchant player aura (auto-detected from CURSE subtype via `isEnchantPlayer()`), creatures enchanted player controls get -1/-1 |
| Curse (enchant player + combat trigger) | `c/CurseOfStalkedPrey.java` | ON_COMBAT_DAMAGE_TO_PLAYER PutCountersOnSourceEffect(1,1,1) — enchants player (auto-detected from CURSE subtype), whenever a creature deals combat damage to enchanted player, put +1/+1 counter on that creature. CombatDamageService checks curses attached to defending player |
| Curse (enchant player + upkeep damage) | `c/CurseOfThePiercedHeart.java` | ENCHANTED_PLAYER_UPKEEP_TRIGGERED DealDamageToPlayersEffect(1, DamageRecipient.ENCHANTED_PLAYER) — enchant player curse; at enchanted player's upkeep, deals 1 damage to that player (targetId baked to the enchanted player by StepTriggerService) |
| Curse (enchant player + upkeep damage = attached count) | `c/CurseOfThirst.java` | ENCHANTED_PLAYER_UPKEEP_TRIGGERED DealDamageToPlayersEffect.enchantedAttachedCount(new PermanentHasSubtypePredicate(CardSubtype.CURSE)) — at enchanted player's upkeep, deals damage equal to the number of permanents (here, Curses) attached to that player matching the predicate |
| Curse (enchant player + upkeep graveyard exile) | `c/CurseOfOblivion.java` | ENCHANTED_PLAYER_UPKEEP_TRIGGERED ExileGraveyardCardsEffect(2, GraveyardExileScope.OWN) — enchant player curse; at enchanted player's upkeep, that player exiles 2 cards from their graveyard (StepTriggerService bakes the enchanted player's id as affectedPlayerId) |
| Curse (enchant player + upkeep mill) | `c/CurseOfTheBloodyTome.java` | ENCHANTED_PLAYER_UPKEEP_TRIGGERED MillEffect(2, TARGET_PLAYER) — enchant player curse; at enchanted player's upkeep, that player mills 2 cards |
| Curse (enchant player + copy their instants/sorceries) | `c/CurseOfEchoes.java` | ON_ANY_PLAYER_CASTS_SPELL CopySpellForEachOtherPlayerEffect(true, StackEntryAllOfPredicate(StackEntryTypeInPredicate(INSTANT_SPELL, SORCERY_SPELL), StackEntryControlledByEnchantedPlayerPredicate)) — enchant player curse; reuses Hive Mind's copy machinery, the filter restricts firing to instant/sorcery spells cast by the enchanted player; each other player *may* copy and may choose new targets |


