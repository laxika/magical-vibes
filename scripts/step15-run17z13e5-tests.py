#!/usr/bin/env python3
"""Step 15, run 17z13e5: the test changes of the enter defaults that replaced the class-keyed enter handlers.

- `EnterTriggerCollectorServiceTest`: the collector takes only the log and query services (the graveyard-targeting,
  predicate and condition services were unused once the graveyard multi-target handlers went); the "up to one target
  card from an opponent's graveyard" case asserted the deleted handler's call of
  `GraveyardTargetingService.handleGraveyardCardsExileETBTargeting`: it asserts the default's graveyard target choice,
  which carries the entering permanent and the source; new case: a "you may pay" from the command zone (Inalla) goes on
  the stack through the watch's default (the watch had no default: only a "you may pay" triggered) with the entering
  permanent recorded.
- Card (`s`): `SuturePriestTest.hexproofOpponentStillLosesLife` (the rules fix: "that player" isn't a target, so the
  ability resolves against an opponent with hexproof).

Run per letter module for the card cases: step15-run17z13e5-tests.py [LETTER...] (s); the service test is always
updated. Idempotent.
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
SERVICE_TEST = ROOT / ("magical-vibes-application/src/test/java/com/github/laxika/magicalvibes/service/trigger/"
                       "EnterTriggerCollectorServiceTest.java")
CARD_TESTS = ROOT / "magical-vibes-card-test"
PKG = "src/test/java/com/github/laxika/magicalvibes/cards"

SERVICE_EDITS = [
    ("""        TriggerCollectorRegistry.scanBean(new EnterTriggerCollectorService(gameLogService, gameQueryService,
                graveyardTargetingService,
                predicateEvaluationService,
                new ConditionEvaluationService(gameQueryService, predicateEvaluationService)), registry);""",
     """        TriggerCollectorRegistry.scanBean(new EnterTriggerCollectorService(gameLogService, gameQueryService),
                registry);"""),
    ("""    @Mock private GraveyardTargetingService graveyardTargetingService;
""", ""),
    ("import com.github.laxika.magicalvibes.service.battlefield.GraveyardTargetingService;\n", ""),
    ("""    @DisplayName("Ally-creature scan queues a graveyard target choice for optional exile")
    void allyCreatureQueuesGraveyardTargeting() {
        var effect = ExileGraveyardCardsEffect.upToOneTargetFromOpponentGraveyard();
        addAllyCreatureTrigger(ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, effect);

        enter(player1Id, enteringCreature(2, 2));
        service.checkAllyCreatureEntersTriggers(gd, player1Id, enteringCreature(2, 2), 0);

        assertThat(gd.stack).isEmpty();
        verify(graveyardTargetingService).handleGraveyardCardsExileETBTargeting(
                eq(gd), eq(player1Id), any(Card.class), eq(List.of(effect)), eq(effect));
    }""",
     """    @DisplayName("Ally-creature scan queues a graveyard target choice for optional exile, with the entering creature")
    void allyCreatureQueuesGraveyardTargeting() {
        var effect = ExileGraveyardCardsEffect.upToOneTargetFromOpponentGraveyard();
        addAllyCreatureTrigger(ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, effect);
        Permanent source = gd.playerBattlefields.get(player1Id).getFirst();
        Card entering = enteringCreature(2, 2);
        Permanent enteringPermanent = new Permanent(entering);
        gd.playerBattlefields.get(player1Id).add(enteringPermanent);

        enter(player1Id, entering);

        assertThat(gd.stack).isEmpty();
        PermanentChoiceContext.SpellGraveyardTargetTrigger choice =
                gd.peekPendingInteraction(PermanentChoiceContext.SpellGraveyardTargetTrigger.class);
        assertThat(choice.effects()).containsExactly(effect);
        assertThat(choice.controllerId()).isEqualTo(player1Id);
        assertThat(choice.sourcePermanentId()).isEqualTo(source.getId());
        assertThat(choice.triggeringPermanentId()).isEqualTo(enteringPermanent.getId());
    }

    @Test
    @DisplayName("A command-zone \\"you may pay\\" goes on the stack through the watch's default, recording the entering creature (Inalla)")
    void commandZoneMayPayRecordsTheEnteringCreature() {
        MayPayManaEffect authored = new MayPayManaEffect("{1}",
                new com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect(true, true)
                        .ofTriggeringPermanent(), "Pay {1}?");
        Card commander = new Card();
        commander.setName("Commander");
        addWatched(commander, ZoneChangeWatch.COMMAND_ZONE_ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD, authored);
        Card entering = enteringCreature(2, 2);
        Permanent enteringPermanent = new Permanent(entering);
        gd.playerBattlefields.get(player1Id).add(enteringPermanent);

        TriggerCandidate candidate = new TriggerCandidate(commander, null, player1Id, Zone.COMMAND,
                commander.getTriggeredAbilities().getFirst());
        service.triggerZoneChange(gd, ZoneChangeWatch.COMMAND_ZONE_ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD,
                candidate, List.of(authored), List.of(new ZoneChangeEvent(entering, player1Id, Zone.HAND,
                        Zone.BATTLEFIELD, ZoneChangeCause.OTHER, new Permanent(enteringPermanent), player1Id)));

        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getEffectsToResolve()).containsExactly(authored);
            assertThat(entry.getCard()).isSameAs(commander);
            assertThat(entry.getSourcePermanentId()).isNull();
            assertThat(entry.getTriggeringPermanentId()).isEqualTo(enteringPermanent.getId());
            assertThat(entry.getTriggeringPermanentControllerId()).isEqualTo(player1Id);
            assertThat(entry.isNonTargeting()).isTrue();
        });
    }"""),
]

SUTURE_PRIEST = "s/SuturePriestTest.java"
SUTURE_CASE = """
    @Test
    @DisplayName("\\"That player\\" isn't a target: an opponent with hexproof still loses 1 life")
    void hexproofOpponentStillLosesLife() {
        harness.addToBattlefield(player1, new SuturePriest());
        harness.addToBattlefield(player2, new com.github.laxika.magicalvibes.cards.l.LeylineOfSanctity());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new SuturePriest()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castCreature(player2, 0);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 19);
    }
"""


def edit_service():
    text = SERVICE_TEST.read_text()
    for old, new in SERVICE_EDITS:
        if new and new in text:
            continue
        if old not in text:
            if not new:
                continue
            sys.exit(f"service test: pattern not found: {old[:70]!r}")
        text = text.replace(old, new, 1)
    if "import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;" not in text:
        text = text.replace("import com.github.laxika.magicalvibes.model.effect.MayEffect;\n",
                            "import com.github.laxika.magicalvibes.model.effect.MayEffect;\n"
                            "import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;\n", 1)
    SERVICE_TEST.write_text(text)


def add_case(rel, case, marker):
    letter = rel.split("/")[0]
    path = CARD_TESTS / letter / PKG / rel
    text = path.read_text()
    if marker in text:
        return
    end = text.rstrip().rfind("}")
    path.write_text(text[:end].rstrip() + "\n" + case + "}\n")
    print("updated", rel)


def main():
    edit_service()
    letters = set(sys.argv[1:]) or {"s"}
    if "s" in letters:
        add_case(SUTURE_PRIEST, SUTURE_CASE, "hexproofOpponentStillLosesLife")


if __name__ == "__main__":
    main()
