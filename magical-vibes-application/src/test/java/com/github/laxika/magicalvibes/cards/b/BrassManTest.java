package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BrassManTest extends BaseCardTest {

    // ===== Doesn't untap during untap step =====

    @Test
    @DisplayName("Tapped Brass Man does not untap during controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent brassMan = addBrassMan(player1, true);

        advanceToNextTurn(player2); // pass turn to player1, triggering player1's untap step

        assertThat(brassMan.isTapped()).isTrue();
    }

    // ===== Upkeep: may pay {1} to untap =====

    @Test
    @DisplayName("Paying {1} during upkeep untaps Brass Man")
    void payingOneUntapsBrassMan() {
        Permanent brassMan = addBrassMan(player1, true);

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities(); // resolve MayPayManaEffect from stack
        harness.handleMayAbilityChosen(player1, true);

        assertThat(brassMan.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the upkeep payment leaves Brass Man tapped")
    void decliningLeavesBrassManTapped() {
        Permanent brassMan = addBrassMan(player1, true);

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(brassMan.isTapped()).isTrue();
    }

    // ===== Helpers =====

    private Permanent addBrassMan(Player player, boolean tapped) {
        Permanent perm = new Permanent(new BrassMan());
        perm.setSummoningSick(false);
        if (tapped) {
            perm.tap();
        }
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // UNTAP -> UPKEEP
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn (untap)
    }
}
