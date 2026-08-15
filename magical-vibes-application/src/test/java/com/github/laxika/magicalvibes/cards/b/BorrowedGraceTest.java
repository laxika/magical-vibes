package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BorrowedGraceTest extends BaseCardTest {

    @Test
    @DisplayName("Power mode gives your creatures +2/+0 until end of turn")
    void powerModeBoostsOwnCreatures() {
        Permanent own = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());
        cast(new int[]{0}, 1, 2);

        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, own)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(2);
    }

    @Test
    @DisplayName("Toughness mode gives your creatures +0/+2 until end of turn")
    void toughnessModeBoostsOwnCreatures() {
        Permanent own = addCreatureReady(player1, new GrizzlyBears());
        cast(new int[]{1}, 1, 2);

        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, own)).isEqualTo(4);
    }

    @Test
    @DisplayName("Both modes require escalate mana and stack both boosts")
    void bothModesEscalateAndResolve() {
        Permanent own = addCreatureReady(player1, new GrizzlyBears());
        cast(new int[]{0, 1}, 2, 3);

        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, own)).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Both modes are rejected without escalate mana")
    void bothModesRequireEscalateMana() {
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BorrowedGrace()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0, 1}, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, int whiteMana, int colorlessMana) {
        harness.setHand(player1, List.of(new BorrowedGrace()));
        harness.addMana(player1, ManaColor.WHITE, whiteMana);
        harness.addMana(player1, ManaColor.COLORLESS, colorlessMana);
        harness.castModalInstantWithModes(player1, 0, 1, 2, modes, List.of());
        harness.passBothPriorities();
    }
}
