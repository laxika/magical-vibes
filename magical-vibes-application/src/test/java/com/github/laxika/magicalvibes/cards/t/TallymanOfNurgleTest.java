package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TallymanOfNurgle.class, Forest.class})
class TallymanOfNurgleTest extends BaseCardTest {

    @Test
    @DisplayName("Does not trigger at the end step when no creature died")
    void doesNotTriggerWithoutCreatureDeath() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addToBattlefield(player1, new TallymanOfNurgle());

        runToEndStep();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Draws a card and loses 1 life after a creature dies")
    void drawsOneAndLosesOneLife() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addToBattlefield(player1, new TallymanOfNurgle());
        gd.creatureDeathCountThisTurn.put(player2.getId(), 1);

        runToEndStep();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Draws seven cards and loses 7 life after seven creatures die")
    void drawsSevenAndLosesSevenLife() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest()));
        harness.addToBattlefield(player1, new TallymanOfNurgle());
        gd.creatureDeathCountThisTurn.put(player2.getId(), 7);

        runToEndStep();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(13);
    }

    private void runToEndStep() {
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
