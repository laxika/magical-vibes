package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RushwoodLegate.class, Forest.class, Island.class})
class RushwoodLegateTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast for free when an opponent controls an Island and you control a Forest")
    void castsForAlternateCost() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new RushwoodLegate()));

        harness.castWithAlternateCost(player1, 0, (UUID) null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rushwood Legate");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Free alternate cast does not spend mana")
    void alternateCostDoesNotSpendMana() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player1, List.of(new RushwoodLegate()));

        harness.castWithAlternateCost(player1, 0, (UUID) null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rushwood Legate");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot use the alternate cost without an opponent-controlled Island")
    void alternateCostRequiresOpponentIsland() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new RushwoodLegate()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, (UUID) null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot use the alternate cost without a controller-controlled Forest")
    void alternateCostRequiresControllerForest() {
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new RushwoodLegate()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, (UUID) null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot use the alternate cost when the required lands are controlled by the wrong players")
    void alternateCostRequiresCorrectControllers() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new RushwoodLegate()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, (UUID) null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be cast normally for its mana cost")
    void castsNormally() {
        harness.castFromHand(player1, new RushwoodLegate(), "{2}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rushwood Legate");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
