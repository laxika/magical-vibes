package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent addReadyLowlands(Player player) {
        harness.addToBattlefield(player, new ThalakosLowlands());
        Permanent perm = gd.playerBattlefields.get(player.getId()).getLast();
        perm.setSummoningSick(false);
        return perm;
    }
}
