package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BorrowedHostilityTest extends BaseCardTest {

    // Modes: 0 = +3/+0, 1 = first strike

    @Test
    @DisplayName("Boost mode: target gets +3/+0 until end of turn")
    void boostModeGivesPlusThreePlusZero() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BorrowedHostility()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0}, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(3);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("First strike mode: target gains first strike until end of turn")
    void firstStrikeModeGrantsFirstStrike() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BorrowedHostility()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{1}, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Both modes: escalate {3} and both effects resolve on the same creature")
    void bothModesEscalateAndResolve() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BorrowedHostility()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(bears.getId(), bears.getId()));
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Both modes without escalate mana is rejected")
    void bothModesWithoutEscalateManaRejected() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BorrowedHostility()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() ->
                harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0, 1},
                        List.of(bears.getId(), bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
