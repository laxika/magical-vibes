package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NearDeathExperienceTest extends BaseCardTest {

    @Test
    @DisplayName("Wins the game during upkeep at exactly 1 life")
    void winsAtExactlyOneLife() {
        harness.addToBattlefield(player1, new NearDeathExperience());
        harness.setLife(player1, 1);

        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Does not trigger during upkeep when life is above 1")
    void doesNotTriggerAboveOneLife() {
        harness.addToBattlefield(player1, new NearDeathExperience());
        harness.setLife(player1, 2);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Checks the exact-life condition again on resolution")
    void conditionIsCheckedAgainOnResolution() {
        harness.addToBattlefield(player1, new NearDeathExperience());
        harness.setLife(player1, 1);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.setLife(player1, 2);
        harness.passBothPriorities();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerOnOpponentsUpkeep() {
        harness.addToBattlefield(player1, new NearDeathExperience());
        harness.setLife(player1, 1);

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }
}
