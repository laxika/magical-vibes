# CARD_PATTERN_INDEX

Purpose: quickly find a reference card for the pattern you're implementing. One or two examples per archetype. All paths relative to `cards/`.

## Lands

| Pattern | Reference | Notes |
|---------|-----------|-------|
| Basic land | `f/Forest.java` | `addEffect(ON_TAP, AwardManaEffect(color))` |
| Pain land | `s/SulfurousSprings.java` | 3 activated abilities: colorless + 2x colored with DealDamageToController |
| Creature land (manland) | `t/TreetopVillage.java` | `setEntersTapped` + ON_TAP mana + AnimateLandEffect ability |
| Creature land + sub-ability | `s/SpawningPool.java` | manland + regenerate with `ONLY_WHILE_CREATURE` restriction |
| Utility land | `q/Quicksand.java` | mana ability + sacrifice-to-debuff ability |

## Spells

| Pattern | Reference | Notes |
|---------|-----------|-------|
| Targeted burn | `s/Shock.java` | SPELL DealDamageToAnyTargetEffect (targeting auto-derived) |
| X burn | `b/Blaze.java` | DealXDamageToAnyTargetEffect |
| Burn + life drain | `e/EssenceDrain.java` | DealDamageToAnyTargetAndGainLifeEffect |
| Multi-target damage | `c/ConeOfFlame.java` | DealOrderedDamageToAnyTargetsEffect |
| Damage all creatures | `p/Pyroclasm.java` | DealDamageToAllCreaturesEffect |
| Pump target | `g/GiantGrowth.java` | BoostTargetCreatureEffect |
| Pump all + keyword | `o/Overrun.java` | BoostAllOwnCreaturesEffect + GrantKeywordEffect |
| Targeted destroy | `t/Terror.java` | DestroyTargetPermanentEffect + target filter |
| Multi-effect removal | `c/Condemn.java` | PutTargetOnBottomOfLibrary + GainLifeEqualToTargetToughness |
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
| Graveyard return | `r/Recollect.java` | ReturnCardFromGraveyardToHandEffect |
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
| ETB may return from GY | `g/Gravedigger.java` | MayEffect(ReturnCreatureFromGraveyardToHandEffect) |
| ETB tokens + ability | `s/SiegeGangCommander.java` | CreateCreatureTokenEffect + activated sac ability |
| ETB copy | `c/Clone.java` | CopyPermanentOnEnterEffect |
| ETB choose color | `v/VoiceOfAll.java` | ProtectionFromChosenColorEffect |
| ETB choose name | `p/PithingNeedle.java` | ChooseCardNameOnEnterEffect + static lock |
| ETB control handoff | `s/SleeperAgent.java` | TargetPlayerGainsControlOfSourceCreatureEffect |
| ETB drawback | `h/HiddenHorror.java` | SacrificeUnlessDiscardCardTypeEffect |
| ETB -1/-1 counters + counter removal ability | `b/BurdenedStoneback.java` | PutCountersOnSourceEffect(-1,-1,2) + RemoveCounterFromSourceCost + GrantKeywordEffect |

## Triggered creatures

| Pattern | Reference | Notes |
|---------|-----------|-------|
| On death | `b/BogardanFirefiend.java` | ON_DEATH DealDamageToTargetCreatureEffect |
| Upkeep sacrifice/discard | `r/RazormaneMasticore.java` | UPKEEP_TRIGGERED + DRAW_TRIGGERED |
| Upkeep bounce | `s/StampedingWildebeests.java` | BounceCreatureOnUpkeepEffect |
| Upkeep token | `v/VerdantForce.java` | EACH_UPKEEP_TRIGGERED CreateCreatureTokenEffect |
| Graveyard upkeep | `s/SqueeGoblinNabob.java` | GRAVEYARD_UPKEEP_TRIGGERED ReturnSelfFromGraveyardToHandEffect |
| Combat damage to player | `t/ThievingMagpie.java` | ON_COMBAT_DAMAGE_TO_PLAYER DrawCardEffect |
| On becomes blocked | `s/SylvanBasilisk.java` | ON_BECOMES_BLOCKED DestroyCreatureBlockingThisEffect |
| On block (mutual destroy) | `l/LoyalSentry.java` | ON_BLOCK DestroyBlockedCreatureAndSelfEffect |
| On damaged creature dies | `s/SengirVampire.java` | ON_DAMAGED_CREATURE_DIES PutPlusOnePlusOneCounterOnSourceEffect |
| Other creature enters | `s/SoulWarden.java` | ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD GainLifeEffect |
| Ally creature dies | `g/GravePact.java` | ON_ALLY_CREATURE_DIES EachOpponentSacrificesCreatureEffect |
| Opponent draws | `u/UnderworldDreams.java` | ON_OPPONENT_DRAWS DealDamageToTargetPlayerEffect |
| Opponent discards | `m/Megrim.java` | ON_OPPONENT_DISCARDS DealDamageToDiscardingPlayerEffect |
| Spell cast trigger | `q/QuirionDryad.java` | ON_ANY_PLAYER_CASTS_SPELL +1/+1 counter |
| May gain life on color spell | `a/AngelsFeather.java` | MayEffect(GainLifeOnColorSpellCastEffect) |
| Land tap trigger | `m/Manabarbs.java` | ON_ANY_PLAYER_TAPS_LAND DealDamageOnLandTapEffect |
| End step self-destruct | `s/SparkElemental.java` | END_STEP_TRIGGERED SacrificeSelfEffect |
| Discarded by opponent | `g/GuerrillaTactics.java` | ON_SELF_DISCARDED_BY_OPPONENT DealDamageToAnyTargetEffect |

## Static permanents

| Pattern | Reference | Notes |
|---------|-----------|-------|
| Subtype lord | `g/GoblinKing.java` | STATIC BoostCreaturesBySubtypeEffect |
| Anthem (all own) | `g/GloriousAnthem.java` | STATIC BoostOwnCreaturesEffect |
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
| Metalcraft keyword | `a/AuriokEdgewright.java` | STATIC MetalcraftKeywordEffect (self-only, 3+ artifacts) |

## Auras

| Pattern | Reference | Notes |
|---------|-----------|-------|
| Lockdown (can't attack/block) | `p/Pacifism.java` | STATIC EnchantedCreatureCantAttackOrBlockEffect |
| Doesn't untap | `d/Dehydration.java` | STATIC EnchantedCreatureDoesntUntapEffect |
| Static boost | `h/HolyStrength.java` | STATIC BoostAttachedCreatureEffect |
| Boost + keyword | `s/SerrasEmbrace.java` | Boost + GrantKeywordEffect(ENCHANTED_CREATURE) |
| Boost per subtype | `b/BlanchwoodArmor.java` | STATIC BoostEnchantedCreaturePerControlledSubtypeEffect |
| Control enchanted | `p/Persuasion.java` | STATIC ControlEnchantedCreatureEffect |
| Grant activated ability | `a/ArcaneTeachings.java` | GrantActivatedAbilityToEnchantedCreatureEffect |
| Redirect damage to creature | `p/Pariah.java` | STATIC RedirectPlayerDamageToEnchantedCreatureEffect |
| Enchanted land mana | `o/Overgrowth.java` | ON_ANY_PLAYER_TAPS_LAND AddManaOnEnchantedLandTapEffect |
| Aura + self-bounce ability | `s/ShimmeringWings.java` | STATIC keyword + activated ReturnSelfToHandEffect |

## Equipment

| Pattern | Reference | Notes |
|---------|-----------|-------|
| Simple boost equip | `l/LeoninScimitar.java` | STATIC BoostAttachedCreatureEffect + EquipEffect ability |
| Boost + keywords equip | `l/LoxodonWarhammer.java` | Boost + GrantKeywordEffect(EQUIPPED_CREATURE) + equip |

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
| Sacrifice subtype for effect | `s/SiegeGangCommander.java` | SacrificeSubtypeCreatureCost + DealDamageToAnyTargetEffect |
| Regenerate | `d/DrudgeSkeletons.java` | `(false, "{B}", RegenerateEffect, false)` |
| Create token | `d/DragonRoost.java` | CreateCreatureTokenEffect |
| Mill target | `m/Millstone.java` | `(true, "{2}", MillTargetPlayerEffect, true)` |
| Mana dork (tap for color) | `b/BirdsOfParadise.java` | `(true, null, AwardAnyColorManaEffect, false)` |
| Mana dork (ON_TAP) | `l/LlanowarElves.java` | addEffect(ON_TAP, AwardManaEffect) |
| Animate self | `c/ChimericStaff.java` | AnimateSelfEffect |
| Sorcery-speed ability | `t/ThrullSurgeon.java` | ActivationTimingRestriction.SORCERY_SPEED |
| Metalcraft exile-flicker | `a/ArgentSphinx.java` | ExileSelfAndReturnAtEndStepEffect + METALCRAFT restriction |
| Metalcraft tap ability | `v/VedalkenCertarch.java` | ActivationTimingRestriction.METALCRAFT + target filter |
| Loyalty (no target) | `a/AjaniOutlandChaperone.java` | `(+1, effects, false, description)` |
| Loyalty (with target filter) | `a/AjaniOutlandChaperone.java` | `(-2, effects, true, description, filter)` |
