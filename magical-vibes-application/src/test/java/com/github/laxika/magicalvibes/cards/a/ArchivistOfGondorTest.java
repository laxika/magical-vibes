package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArchivistOfGondor.class, GrizzlyBears.class})
class ArchivistOfGondorTest extends BaseCardTest {

    @Test
    void commanderCombatDamageMakesItsControllerTheMonarchWhenThereIsNoMonarch() {
        Card commanderCard = new GrizzlyBears();
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
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gd.monarchPlayerId).isNull();
    }

    @Test
    void monarchDrawsAtTheBeginningOfTheirEndStep() {
        harness.addToBattlefield(player1, new ArchivistOfGondor());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        gd.monarchPlayerId = player2.getId();
        int handSize = gd.playerHands.get(player2.getId()).size();

        advanceToEndStep(player2);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSize + 1);
    }

    @Test
    void doesNotDrawAtANonmonarchEndStep() {
        harness.addToBattlefield(player1, new ArchivistOfGondor());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        gd.monarchPlayerId = player1.getId();
        int handSize = gd.playerHands.get(player2.getId()).size();

        advanceToEndStep(player2);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSize);
        assertThat(gd.stack).isEmpty();
    }

    private void advanceToEndStep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
