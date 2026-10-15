package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TangleGolem.class, Forest.class, Mountain.class})
class TangleGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for Forests reduces the casting cost by one per Forest")
    void affinityForForestsReducesCastingCost() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.castFromHand(player1, new TangleGolem(), "{2}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Affinity counts only Forests controlled by the spell's controller")
    void affinityCountsOnlyControlledForests() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new TangleGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Affinity counts tapped Forests")
    void affinityCountsTappedForests() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefieldAndReturn(player1, new Forest()).tap();
        }
        harness.castFromHand(player1, new TangleGolem(), "{2}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Without Forests the full seven mana must be paid")
    void noForestsRequiresFullCost() {
        harness.castFromHand(player1, new TangleGolem(), "{7}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Affinity does not allow casting with less than the reduced cost")
    void insufficientManaForReducedCostCannotCast() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.setHand(player1, List.of(new TangleGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Tangle Golem");
    }

    @Test
    @DisplayName("Seven Forests allow casting without mana")
    void sevenForestsReduceCostToZero() {
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new Forest());
        }

        harness.castFromHand(player1, new TangleGolem(), "{0}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Tangle Golem");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("More than seven Forests do not produce mana from the excess reduction")
    void excessForestsDoNotProduceMana() {
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new Forest());
        }

        harness.castFromHand(player1, new TangleGolem(), "{0}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
