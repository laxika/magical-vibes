package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoyalSubordinate.class, GrizzlyBears.class})
class LoyalSubordinateTest extends BaseCardTest {

    @Test
    void eachOpponentLosesThreeLifeWhileControllingCommander() {
        addCommander(player1);
        harness.addToBattlefield(player1, new LoyalSubordinate());
        int controllerLifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());

        advanceToBeginningOfCombat();

        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 3);
    }

    @Test
    void doesNotTriggerWithoutControllingCommander() {
        harness.addToBattlefield(player1, new LoyalSubordinate());
        int opponentLifeBefore = gd.getLife(player2.getId());

        advanceToBeginningOfCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    private void addCommander(Player player) {
        Card commander = new GrizzlyBears();
        gd.makeCommander(player.getId(), commander);
        harness.addToBattlefield(player, commander);
    }

    private void advanceToBeginningOfCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
