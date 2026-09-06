package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(VampireBats.class)
class VampireBatsTest extends BaseCardTest {

    @Test
    @DisplayName("Ability can be activated twice in one turn")
    void canActivateTwiceInOneTurn() {
        Permanent bats = addReadyVampireBats(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bats.getPowerModifier()).isEqualTo(2);
        assertThat(bats.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Third activation in same turn is rejected")
    void thirdActivationInSameTurnIsRejected() {
        addReadyVampireBats(player1);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no more than 2 times each turn");
    }

    @Test
    @DisplayName("Activation limit resets on a new turn")
    void activationLimitResetsOnNewTurn() {
        addReadyVampireBats(player1);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);

        assertThat(harness.getGameData().stack).hasSize(1);
    }

    @Test
    @DisplayName("Activation requires black mana")
    void activationRequiresBlackMana() {
        Permanent bats = addReadyVampireBats(player1);
        int powerModifierBefore = bats.getPowerModifier();
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(bats.getPowerModifier()).isEqualTo(powerModifierBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability can be activated while summoning sick")
    void canActivateWhileSummoningSick() {
        Permanent bats = harness.addToBattlefieldAndReturn(player1, new VampireBats());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bats.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost expires at the end of the turn")
    void boostExpiresAtEndOfTurn() {
        Permanent bats = addReadyVampireBats(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bats.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bats.getPowerModifier()).isZero();
    }

    private Permanent addReadyVampireBats(Player player) {
        return addCreatureReady(player, new VampireBats());
    }
}
