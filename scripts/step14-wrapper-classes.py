#!/usr/bin/env python3
"""Takes the trigger-only wrapper classes out of the card classes (effect-slot-refactor.md step 14, run 16t).

- SelfExiledFromBattlefieldEffect: "when this is put into exile from the battlefield" is the trigger
  (Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD), null, ObjectRelation.THIS)), in place of "when this leaves the
  battlefield" around the wrapper. Syr Vondam's "while its power is 4 or greater" is the trigger's filter on the source
  as it last existed, as its "dies" half already says.
- TriggeringPermanentControllerConditionalEffect / TriggeringPermanentConditionalEffect around a "whenever a creature
  attacks" or "deals combat damage to an opponent" ability: the trigger's filter ("a creature you control" is
  PermanentControlledBySourceControllerPredicate, judged from the ability's controller), so the same collectors read
  them. The Last Ronin's "attacks alone" is the event condition (CR 702.83a's wording; CR 603.2).
- TriggeringPermanentConditionalEffect as a filter on the dying or entering permanent, a blocking or blocked
  creature, or the creature that deals combat damage to a planeswalker: the trigger's filter, or its combat-only
  trigger (Zagras). Oran-Rief Hydra's and Emeria Shepherd's "if that land is a Forest (Plains) ... instead" were
  judged as the land entered; each branch is an ability whose trigger is filtered by the land, as before.
- TriggeringCardConditionalEffect: a filter on what triggers the ability becomes the trigger's filter; an "if" of an
  effect ("if that creature was a Villain", "if that spell's power is 4 or greater", "if it's a Samurai card", the
  Cemetery cards' re-check of the exiled card's type) becomes ConditionalEffect(TriggeringCardMatches(...)). Merfolk
  Tunnel-Guide's boon takes its filter as the boon's.
- TriggeringArtifactControllerConditionalEffect (Urza's Miter): "an artifact you control ..., if it wasn't sacrificed"
  is Triggers.diesWithoutBeingSacrificed(ARTIFACT, YOU_CONTROL).
- TriggeringAbilityFromNamedLandConditionalEffect (Skophos Maze-Warden): the target trigger's source filter and
  "another creature".
- OncePerTurnTriggerEffect: "this ability triggers only once each turn" is TriggerLimit.ONCE_EACH_TURN (Well Rested's
  granted ability, one instance per granting Aura; Dennick's one ability of two conditions); "for the first time each
  turn" is TriggerLimit.FIRST_TIME_EACH_TURN (Akim, Crawling Sensation, Desert Were-Worm, whose total power is the
  event condition).
- Beifong's Bounty Hunters' unused wrapped constant is removed.

Every replacement is exact and is skipped once its new text is there, so running it again changes nothing. Imports
the new text needs are added; the ones it no longer needs are stripped.

Usage: python3 scripts/step14-wrapper-classes.py [letter ...]
"""
import importlib.util
import os
import re
import sys
from pathlib import Path

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
PKG = "com.github.laxika.magicalvibes"

spec = importlib.util.spec_from_file_location("strip", os.path.join(ROOT, "scripts", "strip-unused-imports.py"))
strip = importlib.util.module_from_spec(spec)
spec.loader.exec_module(strip)

IMPORTS = {
    "Set": "java.util.Set",
    "List": "java.util.List",
    "Zone": PKG + ".model.Zone",
    "ConditionalEffect": PKG + ".model.effect.ConditionalEffect",
    "TriggeringCardMatches": PKG + ".model.condition.TriggeringCardMatches",
    "AttacksAlone": PKG + ".model.condition.AttacksAlone",
    "AttackingCreaturesTotalPowerAtLeast": PKG + ".model.condition.AttackingCreaturesTotalPowerAtLeast",
    "TriggerOptions": PKG + ".model.trigger.TriggerOptions",
    "TriggerLimit": PKG + ".model.trigger.TriggerLimit",
    "TriggerCondition": PKG + ".model.trigger.TriggerCondition",
    "EffectRegistration": PKG + ".model.EffectRegistration",
    "TriggeredAbility": PKG + ".model.TriggeredAbility",
    "BoonTrigger": PKG + ".model.BoonTrigger",
    "PermanentAllOfPredicate": PKG + ".model.filter.PermanentAllOfPredicate",
    "PermanentNotPredicate": PKG + ".model.filter.PermanentNotPredicate",
    "PermanentColorInPredicate": PKG + ".model.filter.PermanentColorInPredicate",
    "PermanentHasSubtypePredicate": PKG + ".model.filter.PermanentHasSubtypePredicate",
    "PermanentHasSupertypePredicate": PKG + ".model.filter.PermanentHasSupertypePredicate",
    "PermanentHasKeywordPredicate": PKG + ".model.filter.PermanentHasKeywordPredicate",
    "PermanentIsArtifactPredicate": PKG + ".model.filter.PermanentIsArtifactPredicate",
    "PermanentIsLandPredicate": PKG + ".model.filter.PermanentIsLandPredicate",
    "PermanentNamedPredicate": PKG + ".model.filter.PermanentNamedPredicate",
    "PermanentIsSourcePermanentPredicate": PKG + ".model.filter.PermanentIsSourcePermanentPredicate",
    "PermanentPowerAtLeastPredicate": PKG + ".model.filter.PermanentPowerAtLeastPredicate",
    "PermanentControlledBySourceControllerPredicate":
        PKG + ".model.filter.PermanentControlledBySourceControllerPredicate",
    "PermanentManaValueGreaterThanSourceCardManaValuePredicate":
        PKG + ".model.filter.PermanentManaValueGreaterThanSourceCardManaValuePredicate",
    "CardSubtypePredicate": PKG + ".model.filter.CardSubtypePredicate",
    "CardSupertype": PKG + ".model.CardSupertype",
}

SELF_EXILED = "new SelfExiledFromBattlefieldEffect("
LEAVES_THIS = "Triggers.leavesBattlefield(ObjectRelation.THIS)"
EXILED_THIS = "Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD), null, ObjectRelation.THIS)"

# file name -> [(old, new)]
MANUAL = {
    # SelfExiledFromBattlefieldEffect whose source condition is a filter.
    "SyrVondamSunstarExemplar.java": [
        ("""
        ConditionalEffect destroyIfPowerAtLeast = new ConditionalEffect(
                new SourcePowerAtLeast(4), new DestroyTargetPermanentEffect());
        target(TargetFilters.nonlandPermanent(), 0, 1)
                .addTrigger(Triggers.dies(new PermanentPowerAtLeastPredicate(4), ObjectRelation.THIS),
                        new DestroyTargetPermanentEffect())
                .bindEffect(destroyIfPowerAtLeast)
                .addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS),
                        new SelfExiledFromBattlefieldEffect(destroyIfPowerAtLeast));""",
         """
        target(TargetFilters.nonlandPermanent(), 0, 1)
                .addTrigger(Triggers.dies(new PermanentPowerAtLeastPredicate(4), ObjectRelation.THIS),
                        new DestroyTargetPermanentEffect())
                .addTrigger(Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD), new PermanentPowerAtLeastPredicate(4),
                                ObjectRelation.THIS),
                        new DestroyTargetPermanentEffect());"""),
    ],
    # TriggeringPermanentControllerConditionalEffect + TriggeringPermanentConditionalEffect.
    "TheRevelationsOfEzio.java": [
        ("""                new TriggeredAbility(Triggers.creatureAttacks(),
                new TriggeringPermanentControllerConditionalEffect(
                        new TriggeringPermanentConditionalEffect(
                                new PermanentHasSubtypePredicate(CardSubtype.ASSASSIN),
                                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE))))));""",
         """                new TriggeredAbility(Triggers.creatureAttacks().withFilter(new PermanentAllOfPredicate(List.of(
                        new PermanentControlledBySourceControllerPredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.ASSASSIN)))),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE))));"""),
    ],
    "HaraldUnitesTheElves.java": [
        ("""                new TriggeredAbility(Triggers.creatureAttacks(),
                new TriggeringPermanentControllerConditionalEffect(
                        new TriggeringPermanentConditionalEffect(
                                new PermanentHasSubtypePredicate(CardSubtype.ELF),
                                new BoostTargetCreatureEffect(-1, -1, opponentCreature))))));""",
         """                new TriggeredAbility(Triggers.creatureAttacks().withFilter(new PermanentAllOfPredicate(List.of(
                        new PermanentControlledBySourceControllerPredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.ELF)))),
                        new BoostTargetCreatureEffect(-1, -1, opponentCreature))));"""),
    ],
    "TheLastRonin.java": [
        ("""                new TriggeredAbility(Triggers.creatureAttacks(),
                new TriggeringPermanentControllerConditionalEffect(
                        new ConditionalEffect(new AttacksAlone(), SequenceEffect.of(
                                new PutCountersOnSourceEffect(1, 1, 3),
                                new GrantKeywordEffect(Set.of(
                                        Keyword.TRAMPLE,
                                        Keyword.LIFELINK,
                                        Keyword.INDESTRUCTIBLE), GrantScope.SELF)))))));""",
         """                new TriggeredAbility(
                        Triggers.creatureAttacks().withFilter(new PermanentControlledBySourceControllerPredicate()),
                        TriggerOptions.DEFAULT.withEventCondition(new AttacksAlone()),
                        SequenceEffect.of(
                                new PutCountersOnSourceEffect(1, 1, 3),
                                new GrantKeywordEffect(Set.of(
                                        Keyword.TRAMPLE,
                                        Keyword.LIFELINK,
                                        Keyword.INDESTRUCTIBLE), GrantScope.SELF)))));"""),
    ],
    "SwordsmanSharpScoundrel.java": [
        ("""        addTrigger(Triggers.creatureAttacks(),
                new TriggeringPermanentControllerConditionalEffect(
                        new TriggeringPermanentConditionalEffect(
                                new PermanentIsEquippedPredicate(),
                                DrawDiscardAndConniveEffect.forEnteringPermanent())));""",
         """        addTrigger(Triggers.creatureAttacks().withFilter(new PermanentAllOfPredicate(List.of(
                        new PermanentControlledBySourceControllerPredicate(), new PermanentIsEquippedPredicate()))),
                DrawDiscardAndConniveEffect.forEnteringPermanent());"""),
    ],
    "NahiriForgedInFury.java": [
        ("""        addTrigger(Triggers.creatureAttacks(), new TriggeringPermanentControllerConditionalEffect(
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsEquippedPredicate(),
                        new ExileTopCardMayPlayThisTurnEffect(
                                1, false, new CardSubtypePredicate(CardSubtype.EQUIPMENT)))));""",
         """        addTrigger(Triggers.creatureAttacks().withFilter(new PermanentAllOfPredicate(List.of(
                        new PermanentControlledBySourceControllerPredicate(), new PermanentIsEquippedPredicate()))),
                new ExileTopCardMayPlayThisTurnEffect(1, false, new CardSubtypePredicate(CardSubtype.EQUIPMENT)));"""),
    ],
    "KestiaTheCultivator.java": [
        ("""        addTrigger(Triggers.creatureAttacks(),
                new TriggeringPermanentControllerConditionalEffect(
                        new TriggeringPermanentConditionalEffect(
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsEnchantedPredicate(),
                                        new PermanentIsEnchantmentPredicate())),
                                new DrawCardEffect(1))));""",
         """        addTrigger(Triggers.creatureAttacks().withFilter(new PermanentAllOfPredicate(List.of(
                        new PermanentControlledBySourceControllerPredicate(),
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsEnchantedPredicate(),
                                new PermanentIsEnchantmentPredicate()))))),
                new DrawCardEffect(1));"""),
    ],
    "EtrataDeadlyFugitive.java": [
        ("""        addTrigger(Triggers.creatureDealsCombatDamageToOpponent(),
                new TriggeringPermanentControllerConditionalEffect(
                        new TriggeringPermanentConditionalEffect(
                                new PermanentHasSubtypePredicate(CardSubtype.ASSASSIN),
                                new CloakTopCardOfDamagedPlayerLibraryEffect()
                        )
                ));""",
         """        addTrigger(Triggers.creatureDealsCombatDamageToOpponent().withFilter(new PermanentAllOfPredicate(List.of(
                        new PermanentControlledBySourceControllerPredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.ASSASSIN)))),
                new CloakTopCardOfDamagedPlayerLibraryEffect());"""),
    ],
    "EdwardKenway.java": [
        ("""        addTrigger(Triggers.creatureDealsCombatDamageToOpponent(),
                new TriggeringPermanentControllerConditionalEffect(
                        new TriggeringPermanentConditionalEffect(
                                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.VEHICLE)),
                                new ExileTopCardOfDamagedPlayerLibraryFaceDownAndGrantCreatureControllerPlayPermissionEffect(false))));""",
         """        addTrigger(Triggers.creatureDealsCombatDamageToOpponent().withFilter(new PermanentAllOfPredicate(List.of(
                        new PermanentControlledBySourceControllerPredicate(),
                        new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.VEHICLE))))),
                new ExileTopCardOfDamagedPlayerLibraryFaceDownAndGrantCreatureControllerPlayPermissionEffect(false));"""),
    ],
    # TriggeringPermanentConditionalEffect alone.
    "ZagrasThiefOfHeartbeats.java": [
        ("""        addTrigger(Triggers.creatureYouControlDealsDamageToPlaneswalker(),
                TriggeringPermanentConditionalEffect.combatDamageOnly(new DestroyTargetPermanentEffect()));""",
         """        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlaneswalker(), new DestroyTargetPermanentEffect());"""),
    ],
    "OranRiefHydra.java": [
        ("""        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL), new TriggeringPermanentConditionalEffect(
                isForest, new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2)),
                new TriggeringPermanentConditionalEffect(
                new PermanentNotPredicate(isForest), new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 1)));""",
         """        addTrigger(Triggers.enters(new PermanentAllOfPredicate(List.of(Triggers.LAND, isForest)),
                        ObjectRelation.YOU_CONTROL),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2));
        addTrigger(Triggers.enters(new PermanentAllOfPredicate(List.of(Triggers.LAND, new PermanentNotPredicate(isForest))),
                        ObjectRelation.YOU_CONTROL),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));"""),
    ],
    "EmeriaShepherd.java": [
        ("""        // permanent card to the battlefield instead. The land is checked as it entered.
        target(new GraveyardCardPredicateTargetFilter(
                nonlandPermanent, GraveyardSearchScope.CONTROLLERS_GRAVEYARD))
                .addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                        new TriggeringPermanentConditionalEffect(plains,
                                new MayEffect(returnToBattlefield,
                                        "Return that card to the battlefield instead?", mayReturnToHand)),
                        new TriggeringPermanentConditionalEffect(new PermanentNotPredicate(plains),
                                mayReturnToHand));""",
         """        // permanent card to the battlefield instead. The land is checked as it entered: each branch is the
        // trigger's filter on it.
        target(new GraveyardCardPredicateTargetFilter(
                nonlandPermanent, GraveyardSearchScope.CONTROLLERS_GRAVEYARD))
                .addTrigger(Triggers.enters(new PermanentAllOfPredicate(List.of(Triggers.LAND, plains)),
                                ObjectRelation.YOU_CONTROL),
                        new MayEffect(returnToBattlefield,
                                "Return that card to the battlefield instead?", mayReturnToHand))
                .addTrigger(Triggers.enters(new PermanentAllOfPredicate(List.of(Triggers.LAND,
                                new PermanentNotPredicate(plains))), ObjectRelation.YOU_CONTROL),
                        mayReturnToHand);"""),
    ],
    "MirrorShield.java": [
        ("""        CardEffect destroyDeathtouchCreature = new TriggeringPermanentConditionalEffect(
                new PermanentHasKeywordPredicate(Keyword.DEATHTOUCH),
                new DestroyCombatOpponentEffect(false), false, true);
        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.blocks(), destroyDeathtouchCreature), GrantScope.EQUIPPED_CREATURE));
        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.becomesBlocked(),
                        destroyDeathtouchCreature), GrantScope.EQUIPPED_CREATURE));""",
         """        // "Whenever a creature with deathtouch blocks or becomes blocked by this creature": the filter is on the
        // creature on the other side of the block.
        PermanentHasKeywordPredicate deathtouch = new PermanentHasKeywordPredicate(Keyword.DEATHTOUCH);
        CardEffect destroyDeathtouchCreature = new DestroyCombatOpponentEffect(false);
        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.blocks().withFilter(deathtouch), destroyDeathtouchCreature),
                GrantScope.EQUIPPED_CREATURE));
        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.becomesBlocked().withFilter(deathtouch), destroyDeathtouchCreature),
                GrantScope.EQUIPPED_CREATURE));"""),
    ],
    "BeifongsBountyHunters.java": [
        ("""    private static final TriggeringPermanentConditionalEffect SELF_DEATH_TRIGGER =
            new TriggeringPermanentConditionalEffect(
                    NONLAND, new EarthbendTargetLandEffect(new SourcePower()));
""", ""),
    ],
    "Agyrem.java": [
        ("""        addTrigger(new TriggerCondition(Triggers.creatureDies(ObjectRelation.ANY), AbilityZones.COMMAND),
                TriggerOptions.DEFAULT, new com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect(
                new com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate(java.util.Set.of(CardColor.WHITE)),
                new RegisterDelayedReturnDyingCreatureUnderControlEffect(
                        false, null, 0, null, null, false, true, false)));
        addTrigger(new TriggerCondition(Triggers.creatureDies(ObjectRelation.ANY), AbilityZones.COMMAND),
                TriggerOptions.DEFAULT, new com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect(
                new com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate(new com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate(java.util.Set.of(CardColor.WHITE))),
                new RegisterDelayedReturnCardFromGraveyardToHandEffect(null)));""",
         """        PermanentColorInPredicate white = new PermanentColorInPredicate(Set.of(CardColor.WHITE));
        addTrigger(new TriggerCondition(Triggers.creatureDies(white, ObjectRelation.ANY), AbilityZones.COMMAND),
                TriggerOptions.DEFAULT, new RegisterDelayedReturnDyingCreatureUnderControlEffect(
                        false, null, 0, null, null, false, true, false));
        addTrigger(new TriggerCondition(Triggers.creatureDies(new PermanentNotPredicate(white), ObjectRelation.ANY),
                        AbilityZones.COMMAND),
                TriggerOptions.DEFAULT, new RegisterDelayedReturnCardFromGraveyardToHandEffect(null));"""),
    ],
    # TriggeringCardConditionalEffect as the trigger's filter.
    "TheMasterGallifreysEnd.java": [
        ("""        addTrigger(Triggers.nontokenCreatureDies(ObjectRelation.YOU_CONTROL),
                new TriggeringCardConditionalEffect(
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.ARTIFACT),
                                new CardTypePredicate(CardType.CREATURE))),
                        new MayEffect(new TheMasterGallifreysEndEffect(),
                                "Exile that creature and make an opponent face a villainous choice?")));""",
         """        addTrigger(Triggers.nontokenCreatureDies(new PermanentIsArtifactPredicate(), ObjectRelation.YOU_CONTROL),
                new MayEffect(new TheMasterGallifreysEndEffect(),
                        "Exile that creature and make an opponent face a villainous choice?"));"""),
    ],
    "TatteredRatter.java": [
        ("""        addTrigger(Triggers.creatureYouControlBecomesBlocked(),
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.RAT),
                        new BoostSelfEffect(2, 0)));""",
         """        addTrigger(Triggers.creatureYouControlBecomesBlocked()
                        .withFilter(new PermanentHasSubtypePredicate(CardSubtype.RAT)),
                new BoostSelfEffect(2, 0));"""),
    ],
    "SpinedSliver.java": [
        ("""        addTrigger(Triggers.creatureBecomesBlocked(),
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.SLIVER),
                        new BoostSelfEffect(new CreaturesBlockingSource(), new CreaturesBlockingSource())));""",
         """        addTrigger(Triggers.creatureBecomesBlocked().withFilter(new PermanentHasSubtypePredicate(CardSubtype.SLIVER)),
                new BoostSelfEffect(new CreaturesBlockingSource(), new CreaturesBlockingSource()));"""),
    ],
    "BerserkMurlodont.java": [
        ("""        addTrigger(Triggers.creatureBecomesBlocked(),
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.BEAST),
                        new BoostSelfEffect(new CreaturesBlockingSource(), new CreaturesBlockingSource())));""",
         """        addTrigger(Triggers.creatureBecomesBlocked().withFilter(new PermanentHasSubtypePredicate(CardSubtype.BEAST)),
                new BoostSelfEffect(new CreaturesBlockingSource(), new CreaturesBlockingSource()));"""),
    ],
    "SenuKeenEyedProtector.java": [
        ("""        addTrigger(new TriggerCondition(Triggers.creatureYouControlAttacksUnblocked(), AbilityZones.EXILE),
                TriggerOptions.DEFAULT,
                new TriggeringCardConditionalEffect(
                        new CardSupertypePredicate(CardSupertype.LEGENDARY),
                        new ReturnSourceCardFromExileToBattlefieldEffect(false, true)));""",
         """        addTrigger(new TriggerCondition(Triggers.creatureYouControlAttacksUnblocked()
                        .withFilter(new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY)), AbilityZones.EXILE),
                TriggerOptions.DEFAULT,
                new ReturnSourceCardFromExileToBattlefieldEffect(false, true));"""),
    ],
    "KamiOfMourning.java": [
        ("""                        new TriggerCondition(Triggers.creatureDies(ObjectRelation.YOU_CONTROL), AbilityZones.GRAVEYARD),
                        new TriggeringCardConditionalEffect(
                                new CardManaValueGreaterThanSourceManaValuePredicate(),
                                new ReturnSourceCardFromGraveyardToBattlefieldEffect(true)))));""",
         """                        new TriggerCondition(Triggers.creatureDies(
                                new PermanentManaValueGreaterThanSourceCardManaValuePredicate(),
                                ObjectRelation.YOU_CONTROL), AbilityZones.GRAVEYARD),
                        new ReturnSourceCardFromGraveyardToBattlefieldEffect(true))));"""),
    ],
    "DisaTheRestless.java": [
        ("""        addTrigger(Triggers.putIntoGraveyardFromAnywhere(Triggers.PERMANENT_CARD, ObjectRelation.ANOTHER_YOU_OWN),
                new TriggeringCardConditionalEffect(
                        new CardAllOfPredicate(List.of(
                                new CardIsPermanentPredicate(),
                                new CardPutIntoGraveyardFromNonBattlefieldThisTurnPredicate(),
                                new CardSubtypePredicate(CardSubtype.LHURGOYF))),
                        new ReturnTriggeringCardFromGraveyardToBattlefieldEffect()));""",
         """        addTrigger(Triggers.putIntoGraveyardFromAnywhereButBattlefield(new PermanentAllOfPredicate(List.of(
                        Triggers.PERMANENT_CARD, new PermanentHasSubtypePredicate(CardSubtype.LHURGOYF))),
                        ObjectRelation.ANOTHER_YOU_OWN),
                new ReturnTriggeringCardFromGraveyardToBattlefieldEffect());"""),
    ],
    # TriggeringCardConditionalEffect as an "if" of an effect.
    "VenomEddieBrock.java": [
        ("""                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.VILLAIN), new DrawCardEffect())));""",
         """                new ConditionalEffect(new TriggeringCardMatches(
                        new CardSubtypePredicate(CardSubtype.VILLAIN)), new DrawCardEffect())));"""),
    ],
    "OvergrowthElemental.java": [
        ("""                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.ELEMENTAL),
                        new PutCountersOnSourceEffect(1, 1, 1))));""",
         """                new ConditionalEffect(new TriggeringCardMatches(
                        new CardSubtypePredicate(CardSubtype.ELEMENTAL)),
                        new PutCountersOnSourceEffect(1, 1, 1))));"""),
    ],
    "OathkeeperTakenosDaisho.java": [
        ("""        addTrigger(Triggers.creatureDies(ObjectRelation.EQUIPPED), new TriggeringCardConditionalEffect(
                new CardSubtypePredicate(CardSubtype.SAMURAI),
                new ReturnDyingCreatureToBattlefieldEffect(false)
        ));""",
         """        addTrigger(Triggers.creatureDies(ObjectRelation.EQUIPPED), new ConditionalEffect(
                new TriggeringCardMatches(new CardSubtypePredicate(CardSubtype.SAMURAI)),
                new ReturnDyingCreatureToBattlefieldEffect(false)));"""),
    ],
    "JensonCarthalionDruidExile.java": [
        ("""                new TriggeringCardConditionalEffect(
                        new CardHasExactlyNColorsPredicate(5),""",
         """                new ConditionalEffect(new TriggeringCardMatches(
                        new CardHasExactlyNColorsPredicate(5)),"""),
    ],
    "EshkiTemursRoar.java": [
        ("""                new TriggeringCardConditionalEffect(
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardPowerAtLeastPredicate(4))),
                        new DrawCardEffect(1)),
                new TriggeringCardConditionalEffect(
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardPowerAtLeastPredicate(6))),
                        new DealDamageToPlayersEffect(new SourcePower(), DamageRecipient.EACH_OPPONENT)));""",
         """                new ConditionalEffect(new TriggeringCardMatches(
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardPowerAtLeastPredicate(4)))),
                        new DrawCardEffect(1)),
                new ConditionalEffect(new TriggeringCardMatches(
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardPowerAtLeastPredicate(6)))),
                        new DealDamageToPlayersEffect(new SourcePower(), DamageRecipient.EACH_OPPONENT)));"""),
    ],
    "CemeteryProtector.java": [
        ("""        CardEffect conditionalHuman = new TriggeringCardConditionalEffect(sharedType, createHuman);""",
         """        CardEffect conditionalHuman = new ConditionalEffect(new TriggeringCardMatches(sharedType), createHuman);"""),
    ],
    "CemeteryGatekeeper.java": [
        ("""        CardEffect conditionalDamage = new TriggeringCardConditionalEffect(sharedType, damage);""",
         """        CardEffect conditionalDamage = new ConditionalEffect(new TriggeringCardMatches(sharedType), damage);"""),
    ],
    "MerfolkTunnelGuide.java": [
        ("""        addTrigger(Triggers.enters(ObjectRelation.THIS), new CreateBoonEffect(3,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.MERFOLK),
                        new ExploreEffect(true, merfolkCreature)),
                BoonTrigger.PERMANENT_ENTERS, targetFilter));""",
         """        addTrigger(Triggers.enters(ObjectRelation.THIS), new CreateBoonEffect(3,
                new ExploreEffect(true, merfolkCreature),
                BoonTrigger.PERMANENT_ENTERS, targetFilter, new CardSubtypePredicate(CardSubtype.MERFOLK)));"""),
    ],
    # TriggeringArtifactControllerConditionalEffect.
    "UrzasMiter.java": [
        ("""        addTrigger(Triggers.dies(Triggers.ARTIFACT, ObjectRelation.ANY),
                new TriggeringArtifactControllerConditionalEffect(
                        new MayPayManaEffect("{3}", new DrawCardEffect(), "Pay {3} to draw a card?"), true));""",
         """        addTrigger(Triggers.diesWithoutBeingSacrificed(Triggers.ARTIFACT, ObjectRelation.YOU_CONTROL),
                new MayPayManaEffect("{3}", new DrawCardEffect(), "Pay {3} to draw a card?"));"""),
    ],
    # TriggeringAbilityFromNamedLandConditionalEffect.
    "SkophosMazeWarden.java": [
        ("""        addTrigger(Triggers.creatureBecomesTargetOfSpellOrAbility(),
                new TriggeringAbilityFromNamedLandConditionalEffect(
                        "Labyrinth of Skophos",
                        new MayEffect(
                                new SourceFightsTargetCreatureEffect(),
                                "Have Skophos Maze-Warden fight that creature?"
                        )
                )
        );""",
         """        // "Whenever another creature becomes the target of an ability of a land you control named Labyrinth of
        // Skophos": the trigger's filter on the targeted creature and its source filter on the ability's permanent.
        addTrigger(Triggers.creatureBecomesTargetOfSpellOrAbility()
                        .withFilter(new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()))
                        .withSource(new PermanentAllOfPredicate(List.of(
                                new PermanentIsLandPredicate(),
                                new PermanentNamedPredicate("Labyrinth of Skophos"),
                                new PermanentControlledBySourceControllerPredicate()))),
                new MayEffect(
                        new SourceFightsTargetCreatureEffect(),
                        "Have Skophos Maze-Warden fight that creature?"));"""),
    ],
    # Cards that only named a wrapper class in a comment or an unused import.
    "WasteNot.java": [
        ("""each gated on the discarded card's type by a
 * {@link TriggeringCardConditionalEffect}.""", """each gated on the discarded card's type by the
 * trigger's filter."""),
        ("import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;\n", ""),
    ],
    "VoldarenBloodcaster.java": [
        ("import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;\n", ""),
    ],
    "DireUndercurrents.java": [
        ("import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;\n", ""),
    ],
    "BindingMummy.java": [
        ("import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;\n", ""),
    ],
    # OncePerTurnTriggerEffect.
    "WellRested.java": [
        ("""                new TriggeredAbility(Triggers.becomesUntapped(),
                OncePerTurnTriggerEffect.perGrant(SequenceEffect.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2),
                        new GainLifeEffect(2),
                        new DrawCardEffect(1)))),""",
         """                new TriggeredAbility(Triggers.becomesUntapped(),
                        TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                        SequenceEffect.of(
                                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2),
                                new GainLifeEffect(2),
                                new DrawCardEffect(1))),"""),
    ],
    "DesertWereWorm.java": [
        ("""        addTrigger(Triggers.youAttack(), new ConditionalEffect(
                new AttackingCreaturesTotalPowerAtLeast(12),
                new OncePerTurnTriggerEffect(SequenceEffect.of(
                        new UntapPermanentsEffect(TapUntapScope.ALL_CREATURES,
                                new PermanentIsAttackingPredicate()),
                        new AdditionalCombatPhaseEffect(1)))));""",
         """        // "Whenever you attack with creatures with total power 12 or greater for the first time each turn": the
        // total power is the event condition, "for the first time each turn" counts the player's attacks.
        addTrigger(Triggers.youAttack(),
                TriggerOptions.DEFAULT.withEventCondition(new AttackingCreaturesTotalPowerAtLeast(12))
                        .withLimit(TriggerLimit.FIRST_TIME_EACH_TURN),
                SequenceEffect.of(
                        new UntapPermanentsEffect(TapUntapScope.ALL_CREATURES,
                                new PermanentIsAttackingPredicate()),
                        new AdditionalCombatPhaseEffect(1)));"""),
    ],
    "DennickPiousApparition.java": [
        ("""        addTrigger(Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN_CREATURE, ObjectRelation.ANOTHER_YOU_OWN),
                investigateOncePerTurn());
        addTrigger(Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN_CREATURE, ObjectRelation.OPPONENT_OWNS),
                investigateOncePerTurn());""",
         """        // One ability: its "triggers only once each turn" covers both conditions.
        addTriggeredAbility(new TriggeredAbility(List.of(
                new TriggerCondition(Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN_CREATURE,
                        ObjectRelation.ANOTHER_YOU_OWN)),
                new TriggerCondition(Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN_CREATURE,
                        ObjectRelation.OPPONENT_OWNS))),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                List.of(new EffectRegistration(CreateTokenEffect.ofClueToken(1)))));"""),
        ("""
    private OncePerTurnTriggerEffect investigateOncePerTurn() {
        return new OncePerTurnTriggerEffect(CreateTokenEffect.ofClueToken(1));
    }
""", ""),
    ],
    "CrawlingSensation.java": [
        ("""                TriggerOptions.ONE_OR_MORE,
                OncePerTurnTriggerEffect.firstLandGraveyardEvent(new CreateTokenEffect("Insect", 1, 1, CardColor.GREEN,
                        List.of(CardSubtype.INSECT), Set.of(), Set.of())));""",
         """                TriggerOptions.ONE_OR_MORE.withLimit(TriggerLimit.FIRST_TIME_EACH_TURN),
                new CreateTokenEffect("Insect", 1, 1, CardColor.GREEN,
                        List.of(CardSubtype.INSECT), Set.of(), Set.of()));"""),
    ],
    "AkimTheSoaringWind.java": [
        ("""                TriggerOptions.ONE_OR_MORE,
                OncePerTurnTriggerEffect.firstTokenCreation(new CreateTokenEffect(
                        1, "Bird", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING), Set.of())));""",
         """                TriggerOptions.ONE_OR_MORE.withLimit(TriggerLimit.FIRST_TIME_EACH_TURN),
                new CreateTokenEffect(
                        1, "Bird", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING), Set.of()));"""),
    ],
}

def matching_paren(text, open_index):
    depth = 0
    i = open_index
    in_string = False
    while i < len(text):
        ch = text[i]
        if in_string:
            if ch == '\\':
                i += 2
                continue
            if ch == '"':
                in_string = False
        elif ch == '"':
            in_string = True
        elif ch == '(':
            depth += 1
        elif ch == ')':
            depth -= 1
            if depth == 0:
                return i
        i += 1
    raise ValueError("unbalanced")


def unwrap_self_exiled(text, name):
    """leavesBattlefield(THIS) ... new SelfExiledFromBattlefieldEffect(X) -> exiledFrom(BATTLEFIELD, THIS) ... X."""
    while SELF_EXILED in text:
        start = text.index(SELF_EXILED)
        close = matching_paren(text, start + len(SELF_EXILED) - 1)
        inner = text[start + len(SELF_EXILED):close]
        leaves = text.rindex(LEAVES_THIS, 0, start)
        between = text[leaves + len(LEAVES_THIS):start]
        if not re.fullmatch(r",\s*", between):
            raise ValueError(name + ": the leaves trigger and the wrapper aren't adjacent: " + between)
        inner_stripped = inner.strip()
        # Keep the inner text's own line breaks; drop the wrapper's.
        text = (text[:leaves] + EXILED_THIS + ", " + inner_stripped + text[close + 1:])
    return text


def add_imports(text, names):
    lines = text.split('\n')
    existing = {l.strip()[len('import '):-1] for l in lines if l.startswith('import ')}
    simple = {fqn.rsplit('.', 1)[1] for fqn in existing}
    wanted = []
    for name in names:
        fqn = IMPORTS[name]
        if fqn not in existing and name not in simple:
            wanted.append('import ' + fqn + ';')
    if not wanted:
        return text
    last = max(i for i, l in enumerate(lines) if l.startswith('import '))
    lines[last + 1:last + 1] = wanted
    return '\n'.join(lines)


def needed_names(text):
    body = text[text.index('public class'):] if 'public class' in text else text
    return [name for name in IMPORTS if re.search(r'\b' + name + r'\b', body)]


def process(path):
    text = path.read_text()
    original = text
    name = path.name
    for old, new in MANUAL.get(name, []):
        if old in text:
            text = text.replace(old, new, 1)
        elif new and new in text:
            pass
        elif not new:
            pass
        else:
            raise ValueError(name + ": neither the old nor the new text is there:\n" + old[:200])
    if SELF_EXILED in text:
        text = unwrap_self_exiled(text, name)
    if text == original:
        return False
    text = add_imports(text, needed_names(text))
    path.write_text(text)
    strip.process(path, True)
    return True


def main():
    letters = sys.argv[1:] or [d for d in sorted(os.listdir(os.path.join(ROOT, "magical-vibes-card")))
                               if len(d) == 1 or d == "shared"]
    changed = []
    for letter in letters:
        base = Path(ROOT, "magical-vibes-card", letter, "src", "main", "java")
        if not base.exists():
            continue
        for path in sorted(base.rglob("*.java")):
            text = path.read_text()
            if path.name in MANUAL or SELF_EXILED in text:
                if process(path):
                    changed.append(path.name)
    print(len(changed), "card classes changed")
    for name in changed:
        print("  " + name)


if __name__ == "__main__":
    main()
