package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.r.RayOfRevelation;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TestOfEndurance.class, RayOfRevelation.class})
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
    @DisplayName("Does not trigger during an opponent's upkeep even when both players have 50 life")
    void doesNotTriggerOnOpponentsUpkeepWhenOpponentAlsoHasFiftyLife() {
        harness.addToBattlefield(player1, new TestOfEndurance());
        harness.setLife(player1, 50);
        harness.setLife(player2, 50);

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
    @Test
    @DisplayName("Wins with more than 50 life and awards the win to the controller")
    void winsAboveFiftyLife() {
        harness.addToBattlefield(player2, new TestOfEndurance());
        harness.setLife(player2, 60);

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Wins if life falls below 50 but returns to 50 before resolution")
    void winsWhenLifeRecoversBeforeResolution() {
        harness.addToBattlefield(player1, new TestOfEndurance());
        harness.setLife(player1, 50);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.setLife(player1, 49);
        harness.setLife(player1, 50);
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Gaining life after upkeep begins does not create a trigger")
    void reachingFiftyDuringUpkeepDoesNotTrigger() {
        harness.addToBattlefield(player1, new TestOfEndurance());
        harness.setLife(player1, 49);

        advanceToUpkeep(player1);
        harness.setLife(player1, 50);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @CardUsed({TestOfEndurance.class, RayOfRevelation.class})
    @DisplayName("Destroying the enchantment does not stop its pending win trigger")
    void winsAfterEnchantmentIsDestroyed() {
        var endurance = harness.addToBattlefieldAndReturn(player1, new TestOfEndurance());
        harness.setLife(player1, 50);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player2, List.of(new RayOfRevelation()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, endurance.getId());

        harness.assertNotOnBattlefield(player1, "Test of Endurance");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }
}
