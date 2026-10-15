package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DaxosOfMeletis;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoyalSubordinate.class, DaxosOfMeletis.class})
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

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    void doesNotTriggerDuringOpponentsCombat() {
        addCommander(player1);
        harness.addToBattlefield(player1, new LoyalSubordinate());
        int opponentLifeBefore = gd.getLife(player2.getId());

        enterBeginningOfCombat(player2);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    void doesNothingIfCommanderLeavesBeforeResolution() {
        addCommander(player1);
        harness.addToBattlefield(player1, new LoyalSubordinate());
        int opponentLifeBefore = gd.getLife(player2.getId());

        enterBeginningOfCombat(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Daxos of Meletis"));
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    void triggerStillResolvesAfterSubordinateLeaves() {
        addCommander(player1);
        harness.addToBattlefield(player1, new LoyalSubordinate());
        int opponentLifeBefore = gd.getLife(player2.getId());

        enterBeginningOfCombat(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Loyal Subordinate"));
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 3);
    }

    @Test
    void controllingOpponentsCommanderDoesNotSatisfyLieutenant() {
        Card commander = new DaxosOfMeletis();
        gd.makeCommander(player2.getId(), commander);
        harness.addToBattlefield(player1, commander);
        harness.addToBattlefield(player1, new LoyalSubordinate());
        int opponentLifeBefore = gd.getLife(player2.getId());

        enterBeginningOfCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    void ordinaryLegendaryCreatureDoesNotSatisfyLieutenant() {
        harness.addToBattlefield(player1, new DaxosOfMeletis());
        harness.addToBattlefield(player1, new LoyalSubordinate());
        int opponentLifeBefore = gd.getLife(player2.getId());

        enterBeginningOfCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    private void addCommander(Player player) {
        Card commander = new DaxosOfMeletis();
        gd.makeCommander(player.getId(), commander);
        harness.addToBattlefield(player, commander);
    }

    private void advanceToBeginningOfCombat() {
        enterBeginningOfCombat(player1);
        resolveAllTriggers();
    }

    private void enterBeginningOfCombat(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player, TurnStep.BEGINNING_OF_COMBAT);
    }
}
