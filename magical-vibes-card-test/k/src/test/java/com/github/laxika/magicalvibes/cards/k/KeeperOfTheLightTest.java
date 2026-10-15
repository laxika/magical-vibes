package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(KeeperOfTheLight.class)
class KeeperOfTheLightTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 3 life when activated targeting an opponent with more life")
    void gainsLife() {
        Permanent keeper = readyKeeper(10, 11);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
        harness.assertLife(player2, 11);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(keeper.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The target life condition is checked only when activating")
    void targetLifeConditionIsCheckedOnlyOnActivation() {
        readyKeeper(10, 11);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.setLife(player2, 9);
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
    }

    @Test
    @DisplayName("Cannot activate without an opponent who has more life")
    void cannotActivateWithoutHigherLifeOpponent() {
        readyKeeper(10, 10);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate when the opponent has less life")
    void cannotActivateWithLowerLifeOpponent() {
        readyKeeper(10, 9);

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

    @Test
    @DisplayName("Life gain still resolves after the controller's life exceeds the opponent's")
    void resolvesAfterControllerGainsLife() {
        readyKeeper(10, 11);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.setLife(player1, 15);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 11);
    }

    @Test
    @DisplayName("Life gain still resolves after Keeper leaves the battlefield")
    void resolvesWithoutSource() {
        Permanent keeper = readyKeeper(10, 11);

        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(keeper);
        gd.playerGraveyards.get(player1.getId()).add(keeper.getCard());
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
        harness.assertLife(player2, 11);
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent keeper = readyKeeper(10, 11);
        keeper.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 10);
        assertThat(keeper.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent keeper = readyKeeper(10, 11);
        keeper.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 10);
    }

    @Test
    @DisplayName("Cannot activate without white mana")
    void cannotActivateWithoutWhiteMana() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 11);
        Permanent keeper = addCreatureReady(player1, new KeeperOfTheLight());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 10);
        assertThat(keeper.isTapped()).isFalse();
    }

    private Permanent readyKeeper(int controllerLife, int opponentLife) {
        harness.setLife(player1, controllerLife);
        harness.setLife(player2, opponentLife);
        Permanent keeper = addCreatureReady(player1, new KeeperOfTheLight());
        harness.addMana(player1, ManaColor.WHITE, 1);
        return keeper;
    }
}
