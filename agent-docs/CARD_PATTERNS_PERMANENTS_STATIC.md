# Card Patterns: Static Permanents & Auras

All paths relative to `cards/`.

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
| Choose subtype + lord boost + cast draw | `v/VanquishersBanner.java` | ON_ENTER_BATTLEFIELD ChooseSubtypeOnEnterEffect + STATIC BoostCreaturesOfChosenSubtypeEffect(1,1) + ON_CONTROLLER_CASTS_SPELL ChosenSubtypeSpellCastTriggerEffect(DrawCardEffect) — choose type on enter, +1/+1 to own creatures of that type, draw on casting creature of that type |
| Shared-type pump | `c/CoatOfArms.java` | STATIC BoostBySharedCreatureTypeEffect |
| Can't block | `s/SpinelessThug.java` | STATIC CantBlockEffect |
| Must attack | `b/BloodrockCyclops.java` | STATIC MustAttackEffect |
| Evasion (blocked only by) | `e/ElvenRiders.java` | STATIC CanBeBlockedOnlyByFilterEffect |
| Block limit | `s/StalkingTiger.java` | STATIC CanBeBlockedByAtMostNCreaturesEffect |
| Unblockable | `p/PhantomWarrior.java` | STATIC CantBeBlockedEffect |
| Conditional unblockable | `s/ScrapdiverSerpent.java` | STATIC CantBeBlockedIfDefenderControlsMatchingPermanentEffect |
| Grant unblockable to own creatures | `t/TetsukoUmezawaFugitive.java` | STATIC GrantEffectEffect(CantBeBlockedEffect, ALL_OWN_CREATURES, PermanentAnyOfPredicate(power/toughness filter)) |
| Attack restriction (defender) | `s/SeaMonster.java` | STATIC CantAttackUnlessDefenderControlsMatchingPermanentEffect |
| Attack restriction (controller controls) | `d/DesperateCastaways.java` | STATIC CantAttackUnlessControllerControlsMatchingPermanentEffect(predicate, desc) — can't attack unless controller controls a matching permanent |
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
| Tax matching spells (all players) | `t/ThaliaGuardianOfThraben.java` | STATIC IncreaseSpellCostEffect(CardNotPredicate(CardTypePredicate(CREATURE)), 1) — symmetric, affects all players |
| Enters tapped | `r/RootMaze.java` | STATIC EnterPermanentsOfTypesTappedEffect |
| Opponent creatures enter tapped + haste lord | `u/UrabraskTheHidden.java` | STATIC GrantKeywordEffect(HASTE, OWN_CREATURES) + EnterPermanentsOfTypesTappedEffect(CREATURE, opponentsOnly=true) |
| P/T = lands | `m/MolimoMaroSorcerer.java` | STATIC PowerToughnessEqualToControlledLandCountEffect |
| P/T = creatures | `s/ScionOfTheWild.java` | STATIC PowerToughnessEqualToControlledCreatureCountEffect |
| P/T = subtype | `n/Nightmare.java` | STATIC PowerToughnessEqualToControlledSubtypeCountEffect |
| P/T = GY creatures | `m/Mortivore.java` | STATIC PowerToughnessEqualToCreatureCardsInAllGraveyardsEffect |
| P/T = hand size + draw trigger | `p/PsychosisCrawler.java` | STATIC PowerToughnessEqualToCardsInHandEffect + ON_CONTROLLER_DRAWS EachOpponentLosesLifeEffect |
| Self boost per lands + GY lands | `m/MultaniYavimayasAvatar.java` | STATIC BoostSelfPerControlledPermanentEffect(1, 1, PermanentIsLandPredicate) + BoostSelfPerCardsInControllerGraveyardEffect(CardTypePredicate(LAND), 1, 1) — +1/+1 per land you control and per land card in your graveyard |
| Gain GY creature abilities | `n/NecroticOoze.java` | STATIC GainActivatedAbilitiesOfCreatureCardsInAllGraveyardsEffect — selfOnly, gains all activated abilities of all creature cards in all graveyards |
| +1/+1 per same name | `r/RelentlessRats.java` | STATIC BoostByOtherCreaturesWithSameNameEffect |
| +1/+0 per other subtype you control | `r/RatColony.java` | STATIC BoostSelfPerOtherControlledSubtypeEffect(RAT, 1, 0) |
| Cost reduction | `a/AvatarOfMight.java` | STATIC ReduceOwnCastCostIfOpponentControlsMoreCreaturesEffect |
| Subtype cost reduction | `d/DanithaCapashenParagon.java` | STATIC ReduceOwnCastCostForSubtypeEffect(Set.of(AURA, EQUIPMENT), 1) — from battlefield permanent |
| Creature mana only | `m/MyrSuperion.java` | setRequiresCreatureMana(true) — can only be cast with mana produced by creatures |
| No max hand size | `s/Spellbook.java` | STATIC NoMaximumHandSizeEffect |
| Toughness as combat damage (controller) | `b/BelligerentBrontodon.java` | STATIC AssignCombatDamageWithToughnessEffect(ALL_OWN_CREATURES) — all your creatures assign combat damage equal to toughness |
| Double damage (global) | `f/FurnaceOfRath.java` | STATIC DoubleDamageEffect |
| Double damage (controller's all sources) | `a/AngrathsMarauders.java` | STATIC DoubleControllerDamageEffect(null, true) — doubles all damage from sources you control (combat, spells, abilities) |
| Double damage (controller's spells by color) | `f/FireServant.java` | STATIC DoubleControllerDamageEffect(AllOf[TypeIn(INSTANT,SORCERY), ColorIn(RED)], false) — doubles only red instant/sorcery damage |
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
| Life threshold transform (activated) | `c/ChaliceOfLife.java` | ActivatedAbility(tap, [GainLifeEffect(1), ControllerLifeThresholdConditionalEffect(30, TransformSelfEffect())]) — gain 1 life then transform if 30+ life |
| Graveyard card threshold boost + keyword | `g/GhituLavarunner.java` | STATIC ControllerGraveyardCardThresholdConditionalEffect(2, instant/sorcery filter, StaticBoostEffect(1, 0, [HASTE], SELF)) — +1/+0 and haste as long as 2+ instant/sorcery cards in controller's graveyard |
| Controls-subtype self-boost | `t/TeferisSentinel.java` | STATIC ControlsSubtypeConditionalEffect(TEFERI, StaticBoostEffect(4, 0, SELF)) — +4/+0 as long as you control a Teferi planeswalker |
| Controls-subtype self-boost + unblockable | `j/JacesSentinel.java` | STATIC ControlsSubtypeConditionalEffect(JACE, StaticBoostEffect(1, 0, SELF)) + ControlsSubtypeConditionalEffect(JACE, GrantEffectEffect(CantBeBlockedEffect, SELF)) — +1/+0 and can't be blocked as long as you control a Jace planeswalker |

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
| Enchanted land grant mana ability + ETB counter | `n/NewHorizons.java` | STATIC GrantActivatedAbilityEffect(AwardAnyColorManaEffect(2), ENCHANTED_PERMANENT) + ON_ENTER_BATTLEFIELD PutPlusOnePlusOneCounterOnTargetCreatureEffect(1) — multi-target aura: first target is land (aura attachment), second is creature (ETB +1/+1 counter). Uses GrantScope.ENCHANTED_PERMANENT for non-creature aura targets |
| Enchanted land becomes basic type | `e/EvilPresence.java` | STATIC EnchantedPermanentBecomesTypeEffect(SWAMP) — land loses all land types/abilities, becomes the new basic land type |
| Aura with tap enchanted creature ability | `b/BurdenOfGuilt.java` | Activated TapEnchantedCreatureEffect — aura's own ability pays {1} to tap the enchanted creature (no targeting, finds creature via attachedTo) |
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


