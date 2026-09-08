package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhyrexianIronfootTest extends BaseCardTest {

    @Test
    @DisplayName("Phyrexian Ironfoot does not untap during its controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent ironfoot = addIronfootReady(player1);
        ironfoot.tap();

        advanceToNextTurn(player2);

        assertThat(ironfoot.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying generic and snow mana untaps Phyrexian Ironfoot")
    void payingGenericAndSnowManaUntapsIt() {
        Permanent ironfoot = addIronfootReady(player1);
        ironfoot.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ironfoot.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowManaTotal()).isZero();
    }

    @Test
    @DisplayName("Regular mana cannot pay Phyrexian Ironfoot's snow activation cost")
    void regularManaCannotPaySnowCost() {
        addIronfootReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addIronfootReady(Player player) {
        Permanent ironfoot = new Permanent(new PhyrexianIronfoot());
        ironfoot.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(ironfoot);
        return ironfoot;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
