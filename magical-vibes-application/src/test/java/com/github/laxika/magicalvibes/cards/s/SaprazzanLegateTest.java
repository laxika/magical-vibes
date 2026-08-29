package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SaprazzanLegateTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast for free when an opponent controls a Mountain and you control an Island")
    void castsForAlternateCost() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new SaprazzanLegate()));

        harness.castWithAlternateCost(player1, 0, (UUID) null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Saprazzan Legate");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot use the alternate cost without an opponent-controlled Mountain")
    void alternateCostRequiresOpponentMountain() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new SaprazzanLegate()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, (UUID) null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot use the alternate cost without a controller-controlled Island")
    void alternateCostRequiresControllerIsland() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new SaprazzanLegate()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, (UUID) null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be cast normally for its mana cost")
    void castsNormally() {
        harness.setHand(player1, List.of(new SaprazzanLegate()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Saprazzan Legate");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
