package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkyshroudCutterTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast for its alternate cost when you control a Forest")
    void castsForAlternateCost() {
        harness.addToBattlefield(player1, new Forest());
        int opponentLife = gd.playerLifeTotals.get(player2.getId());
        harness.setHand(player1, List.of(new SkyshroudCutter()));

        harness.castWithAlternateCost(player1, 0, (UUID) null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Skyshroud Cutter");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLife + 5);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot use the alternate cost without controlling a Forest")
    void alternateCostRequiresForest() {
        harness.setHand(player1, List.of(new SkyshroudCutter()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, (UUID) null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be cast normally for its mana cost")
    void castsNormally() {
        harness.setHand(player1, List.of(new SkyshroudCutter()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Skyshroud Cutter");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
