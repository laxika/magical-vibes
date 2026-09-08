package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FathomFleetBoarderTest extends BaseCardTest {

    @Test
    @DisplayName("ETB trigger makes you lose 2 life without another Pirate")
    void losesLifeWithoutAnotherPirate() {
        int lifeBefore = gd.getLife(player1.getId());

        castFathomFleetBoarder();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("ETB trigger does not make you lose life when you control another Pirate")
    void doesNotLoseLifeWithAnotherPirate() {
        harness.addToBattlefield(player1, new FathomFleetCaptain());
        int lifeBefore = gd.getLife(player1.getId());

        castFathomFleetBoarder();
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("An opponent's Pirate does not satisfy the ETB condition")
    void opponentPirateDoesNotCount() {
        harness.addToBattlefield(player2, new FathomFleetCaptain());
        int lifeBefore = gd.getLife(player1.getId());

        castFathomFleetBoarder();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("ETB condition is checked when the trigger resolves")
    void checksConditionAtResolution() {
        int lifeBefore = gd.getLife(player1.getId());

        castFathomFleetBoarder();
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new FathomFleetCaptain());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    private void castFathomFleetBoarder() {
        harness.setHand(player1, List.of(new FathomFleetBoarder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0);
    }
}
