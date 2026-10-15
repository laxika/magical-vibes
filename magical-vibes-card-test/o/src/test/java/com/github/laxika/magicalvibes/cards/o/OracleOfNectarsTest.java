package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OracleOfNectars.class})
class OracleOfNectarsTest extends BaseCardTest {

    @Test
    @DisplayName("Activating with X=3 gains 3 life and taps the creature")
    void gainsXLife() {
        harness.setLife(player1, 20);
        Permanent oracle = addReadyOracle(player1);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, 3, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        assertThat(oracle.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating with X=0 gains no life")
    void gainsNoLifeWithZero() {
        harness.setLife(player1, 20);
        addReadyOracle(player1);

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    void tapCostIsPaidBeforeLifeGainResolves() {
        harness.setLife(player1, 20);
        Permanent oracle = addReadyOracle(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 4, null);

        assertThat(oracle.isTapped()).isTrue();
        harness.assertLife(player1, 20);
        assertThat(harness.getGameData().stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent oracle = harness.addToBattlefieldAndReturn(player1, new OracleOfNectars());
        oracle.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(oracle.isTapped()).isFalse();
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent oracle = addReadyOracle(player1);
        oracle.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    void cannotChooseXGreaterThanAvailableMana() {
        Permanent oracle = addReadyOracle(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(oracle.isTapped()).isFalse();
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    void abilityResolvesAfterSourceLeavesBattlefield() {
        harness.setLife(player1, 20);
        Permanent oracle = addReadyOracle(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, 0, 3, null);
        harness.getGameData().playerBattlefields.get(player1.getId()).remove(oracle);
        harness.getGameData().playerGraveyards.get(player1.getId()).add(oracle.getCard());

        harness.passBothPriorities();

        harness.assertLife(player1, 23);
    }

    private Permanent addReadyOracle(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new OracleOfNectars());
        perm.setSummoningSick(false);
        return perm;
    }
}
