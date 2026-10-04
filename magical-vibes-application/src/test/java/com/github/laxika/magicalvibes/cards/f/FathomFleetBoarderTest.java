package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FathomFleetBoarder.class})
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
        harness.addToBattlefield(player1, new FathomFleetBoarder());
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
        harness.addToBattlefield(player2, new FathomFleetBoarder());
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
        harness.addToBattlefield(player1, new FathomFleetBoarder());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Lose life if the other Pirate leaves before the trigger resolves")
    void losesLifeWhenOtherPirateLeavesBeforeResolution() {
        var otherPirate = harness.addToBattlefieldAndReturn(player1, new FathomFleetBoarder());
        int lifeBefore = gd.getLife(player1.getId());

        castFathomFleetBoarder();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(otherPirate);
        gd.playerGraveyards.get(player1.getId()).add(otherPirate.getCard());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("The trigger still makes you lose life if the Boarder leaves the battlefield")
    void triggerResolvesAfterSourceLeaves() {
        int lifeBefore = gd.getLife(player1.getId());

        castFathomFleetBoarder();
        harness.passBothPriorities();
        var boarder = gd.playerBattlefields.get(player1.getId()).getFirst();
        gd.playerBattlefields.get(player1.getId()).remove(boarder);
        gd.playerGraveyards.get(player1.getId()).add(boarder.getCard());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    private void castFathomFleetBoarder() {
        harness.castFromHand(player1, new FathomFleetBoarder(), "{2}{B}");
    }
}
