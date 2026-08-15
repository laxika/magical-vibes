package com.github.laxika.magicalvibes.cards.g;

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

class GrimMonolithTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Grim Monolith produces three colorless mana")
    void tappingProducesThreeColorlessMana() {
        addReadyMonolith(player1, false);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Grim Monolith does not untap during its controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent monolith = addReadyMonolith(player1, true);

        advanceToNextTurn(player2);

        assertThat(monolith.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying {4} untaps Grim Monolith")
    void payingFourUntapsMonolith() {
        Permanent monolith = addReadyMonolith(player1, true);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(monolith.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Grim Monolith's untap ability requires {4}")
    void cannotUntapWithoutFourMana() {
        addReadyMonolith(player1, true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addReadyMonolith(Player player, boolean tapped) {
        Permanent monolith = new Permanent(new GrimMonolith());
        monolith.setSummoningSick(false);
        if (tapped) {
            monolith.tap();
        }
        gd.playerBattlefields.get(player.getId()).add(monolith);
        return monolith;
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
