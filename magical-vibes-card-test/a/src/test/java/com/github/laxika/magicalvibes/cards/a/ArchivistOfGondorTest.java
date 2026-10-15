package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FaramirStewardOfGondor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArchivistOfGondor.class, FaramirStewardOfGondor.class})
class ArchivistOfGondorTest extends BaseCardTest {

    @Test
    void commanderCombatDamageMakesItsControllerTheMonarchWhenThereIsNoMonarch() {
        Card commanderCard = new FaramirStewardOfGondor();
        gd.makeCommander(player1.getId(), commanderCard);
        addCreatureReady(player1, new ArchivistOfGondor());
        addCreatureReady(player1, commanderCard);

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void noncommanderCombatDamageDoesNotMakeItsControllerTheMonarch() {
        addCreatureReady(player1, new ArchivistOfGondor());
        addCreatureReady(player1, new ArchivistOfGondor());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gd.monarchPlayerId).isNull();
    }

    @Test
    void monarchDrawsFromBothArchivistAndTheInherentMonarchAbility() {
        harness.addToBattlefield(player1, new ArchivistOfGondor());
        harness.setLibrary(player2, List.of(new ArchivistOfGondor(), new ArchivistOfGondor()));
        gd.monarchPlayerId = player2.getId();
        int handSize = gd.playerHands.get(player2.getId()).size();

        advanceToEndStep(player2);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSize + 2);
    }

    @Test
    void doesNotDrawAtANonmonarchEndStep() {
        harness.addToBattlefield(player1, new ArchivistOfGondor());
        harness.setLibrary(player2, List.of(new ArchivistOfGondor()));
        gd.monarchPlayerId = player1.getId();
        int handSize = gd.playerHands.get(player2.getId()).size();

        advanceToEndStep(player2);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSize);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsCommanderControlledByYouDoesNotMakeYouMonarch() {
        Card commanderCard = new FaramirStewardOfGondor();
        gd.makeCommander(player2.getId(), commanderCard);
        addCreatureReady(player1, new ArchivistOfGondor());
        addCreatureReady(player1, commanderCard);

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gd.monarchPlayerId).isNull();
    }

    @Test
    void yourCommanderControlledByAnOpponentStillMakesYouMonarch() {
        Card commanderCard = new FaramirStewardOfGondor();
        gd.makeCommander(player1.getId(), commanderCard);
        harness.addToBattlefield(player1, new ArchivistOfGondor());
        addCreatureReady(player2, commanderCard);

        declareAttackers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void commanderDamageDoesNotTriggerArchivistWhenAMonarchAlreadyExists() {
        Card commanderCard = new FaramirStewardOfGondor();
        gd.makeCommander(player1.getId(), commanderCard);
        addCreatureReady(player1, new ArchivistOfGondor());
        addCreatureReady(player1, commanderCard);
        gd.monarchPlayerId = player1.getId();

        declareAttackersAndPrepareBlockers(List.of(1));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of()));
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        assertThat(gd.currentStep).isEqualTo(TurnStep.COMBAT_DAMAGE);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void noMonarchMeansNoEndStepDraw() {
        harness.addToBattlefield(player1, new ArchivistOfGondor());
        harness.setLibrary(player2, List.of(new ArchivistOfGondor()));
        int handSize = gd.playerHands.get(player2.getId()).size();

        advanceToEndStep(player2);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSize);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void becomingMonarchBeforeCommanderTriggerResolvesPreventsTakingTheCrown() {
        Card commanderCard = new FaramirStewardOfGondor();
        gd.makeCommander(player1.getId(), commanderCard);
        addCreatureReady(player1, new ArchivistOfGondor());
        addCreatureReady(player1, commanderCard);

        declareAttackersAndPrepareBlockers(List.of(1));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of()));
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        assertThat(gd.stack).hasSize(1);
        gd.monarchPlayerId = player2.getId();
        resolveAllTriggers();

        assertThat(gd.monarchPlayerId).isEqualTo(player2.getId());
    }

    @Test
    void endStepDrawStillGoesToThePlayerWhoWasMonarchWhenItTriggered() {
        harness.addToBattlefield(player1, new ArchivistOfGondor());
        harness.setLibrary(player2, List.of(new ArchivistOfGondor(), new ArchivistOfGondor()));
        gd.monarchPlayerId = player2.getId();
        int originalMonarchHandSize = gd.playerHands.get(player2.getId()).size();
        int newMonarchHandSize = gd.playerHands.get(player1.getId()).size();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);

        gd.monarchPlayerId = player1.getId();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(originalMonarchHandSize + 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(newMonarchHandSize);
    }

    private void advanceToEndStep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
