package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KeeperOfTheFlameTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage when activated targeting an opponent with more life")
    void dealsDamage() {
        Permanent keeper = readyKeeper(10, 11);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 9);
        assertThat(keeper.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The target life condition is checked only when activating")
    void targetLifeConditionIsCheckedOnlyOnActivation() {
        readyKeeper(10, 11);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.setLife(player2, 9);
        harness.passBothPriorities();

        harness.assertLife(player2, 7);
    }

    @Test
    @DisplayName("Cannot activate without an opponent who has more life")
    void cannotActivateWithoutHigherLifeOpponent() {
        readyKeeper(10, 10);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        readyKeeper(10, 11);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent readyKeeper(int controllerLife, int opponentLife) {
        harness.setLife(player1, controllerLife);
        harness.setLife(player2, opponentLife);
        Permanent keeper = addCreatureReady(player1, new KeeperOfTheFlame());
        harness.addMana(player1, ManaColor.RED, 1);
        return keeper;
    }
}
