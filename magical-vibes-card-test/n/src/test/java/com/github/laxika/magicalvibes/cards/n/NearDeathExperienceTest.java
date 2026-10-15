package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NearDeathExperience.class})
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

    @Test
    @DisplayName("Wins if life returns to 1 before the upkeep trigger resolves")
    void winsWhenLifeReturnsToOneBeforeResolution() {
        harness.addToBattlefield(player1, new NearDeathExperience());
        harness.setLife(player1, 1);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.setLife(player1, 2);
        harness.setLife(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("The upkeep trigger still wins after the enchantment leaves the battlefield")
    void winsAfterSourceLeavesBattlefield() {
        NearDeathExperience card = new NearDeathExperience();
        harness.addToBattlefield(player1, card);
        harness.setLife(player1, 1);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).clear();
        gd.playerGraveyards.get(player1.getId()).add(card);
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("The second player wins on their own upkeep at exactly 1 life")
    void secondPlayerWinsOnOwnUpkeep() {
        harness.addToBattlefield(player2, new NearDeathExperience());
        harness.setLife(player2, 1);

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }
}
