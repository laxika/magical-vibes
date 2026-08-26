package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TestOfEndurance.class)
class TestOfEnduranceTest extends BaseCardTest {

    @Test
    @DisplayName("Wins the game at upkeep with exactly 50 life")
    void winsWithExactlyFiftyLife() {
        harness.addToBattlefield(player1, new TestOfEndurance());
        harness.setLife(player1, 50);

        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Does not trigger at upkeep with less than 50 life")
    void doesNotTriggerBelowFiftyLife() {
        harness.addToBattlefield(player1, new TestOfEndurance());
        harness.setLife(player1, 49);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerOnOpponentsUpkeep() {
        harness.addToBattlefield(player1, new TestOfEndurance());
        harness.setLife(player1, 50);

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Does not win if life drops below 50 before resolution")
    void conditionIsCheckedAgainOnResolution() {
        harness.addToBattlefield(player1, new TestOfEndurance());
        harness.setLife(player1, 50);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.setLife(player1, 49);
        harness.passBothPriorities();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }
}
