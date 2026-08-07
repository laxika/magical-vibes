package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CinderMarshTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for colorless adds {C} and the land untaps normally")
    void tapForColorlessDoesNotSkipUntap() {
        Permanent marsh = addReadyMarsh(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(marsh.isTapped()).isTrue();
        assertThat(marsh.getSkipUntapCount()).isZero();
        // Mana ability — does not use the stack
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping for black adds {B} and the land skips its next untap")
    void tapForBlackSkipsUntap() {
        Permanent marsh = addReadyMarsh(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(marsh.isTapped()).isTrue();
        assertThat(marsh.getSkipUntapCount()).isGreaterThan(0);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping for red adds {R} and the land skips its next untap")
    void tapForRedSkipsUntap() {
        Permanent marsh = addReadyMarsh(player1);

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(marsh.isTapped()).isTrue();
        assertThat(marsh.getSkipUntapCount()).isGreaterThan(0);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The land stays tapped through the next untap step, then untaps the turn after")
    void staysTappedForOneUntapStep() {
        Permanent marsh = addReadyMarsh(player1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        advanceToNextTurn(player1);
        advanceToNextTurn(player2);
        assertThat(marsh.isTapped()).isTrue();
        assertThat(marsh.getSkipUntapCount()).isZero();

        advanceToNextTurn(player1);
        advanceToNextTurn(player2);
        assertThat(marsh.isTapped()).isFalse();
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

    private Permanent addReadyMarsh(Player player) {
        harness.addToBattlefield(player, new CinderMarsh());
        Permanent perm = gd.playerBattlefields.get(player.getId()).getLast();
        perm.setSummoningSick(false);
        return perm;
    }
}
