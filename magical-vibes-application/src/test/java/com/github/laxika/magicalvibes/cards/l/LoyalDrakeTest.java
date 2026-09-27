package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoyalDrake.class, GrizzlyBears.class})
class LoyalDrakeTest extends BaseCardTest {

    @Test
    void drawsAtBeginningOfCombatWhileControllingCommander() {
        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        harness.addToBattlefield(player1, commander);
        harness.addToBattlefield(player1, new LoyalDrake());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        advanceToBeginningOfCombat();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    void doesNotDrawWithoutControllingCommander() {
        harness.addToBattlefield(player1, new LoyalDrake());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        advanceToBeginningOfCombat();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    private void advanceToBeginningOfCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
