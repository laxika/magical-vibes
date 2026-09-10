package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ThalakosLowlands.class)
class ThalakosLowlandsTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for colorless adds {C} and the land untaps normally")
    void tapForColorlessDoesNotSkipUntap() {
        Permanent lowlands = addReadyLowlands(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(lowlands.isTapped()).isTrue();
        assertThat(lowlands.getSkipUntapCount()).isZero();
        // Mana ability — does not use the stack
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping for white adds {W} and the land skips its next untap")
    void tapForWhiteSkipsUntap() {
        Permanent lowlands = addReadyLowlands(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(lowlands.isTapped()).isTrue();
        assertThat(lowlands.getSkipUntapCount()).isGreaterThan(0);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping for blue adds {U} and the land skips its next untap")
    void tapForBlueSkipsUntap() {
        Permanent lowlands = addReadyLowlands(player1);

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(lowlands.isTapped()).isTrue();
        assertThat(lowlands.getSkipUntapCount()).isGreaterThan(0);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A colored activation only keeps the activated land tapped")
    void coloredActivationOnlySkipsSourceLand() {
        Permanent activatedLowlands = addReadyLowlands(player1);
        Permanent otherLowlands = addReadyLowlands(player1);
        otherLowlands.tap();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.performUntapStep(player1);

        assertThat(activatedLowlands.isTapped()).isTrue();
        assertThat(activatedLowlands.getSkipUntapCount()).isZero();
        assertThat(otherLowlands.isTapped()).isFalse();

        harness.performUntapStep(player1);

        assertThat(activatedLowlands.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The land stays tapped through the next untap step, then untaps the turn after")
    void staysTappedForOneUntapStep() {
        Permanent lowlands = addReadyLowlands(player1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        advanceToNextTurn(player1);
        advanceToNextTurn(player2);
        assertThat(lowlands.isTapped()).isTrue();
        assertThat(lowlands.getSkipUntapCount()).isZero();

        advanceToNextTurn(player1);
        advanceToNextTurn(player2);
        assertThat(lowlands.isTapped()).isFalse();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        Player nextActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.passUntil(nextActivePlayer, TurnStep.UPKEEP);
    }

    private Permanent addReadyLowlands(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ThalakosLowlands());
        perm.setSummoningSick(false);
        return perm;
    }
}
