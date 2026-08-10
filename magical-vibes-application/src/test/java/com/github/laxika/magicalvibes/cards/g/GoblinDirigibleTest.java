package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoblinDirigibleTest extends BaseCardTest {

    @Test
    void doesNotUntapDuringUntapStep() {
        Permanent dirigible = addGoblinDirigible(player1, true);

        advanceToNextTurn(player2);

        assertThat(dirigible.isTapped()).isTrue();
    }

    @Test
    void payingFourDuringUpkeepUntapsDirigible() {
        Permanent dirigible = addGoblinDirigible(player1, true);

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(dirigible.isTapped()).isFalse();
    }

    @Test
    void decliningUpkeepPaymentLeavesDirigibleTapped() {
        Permanent dirigible = addGoblinDirigible(player1, true);

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(dirigible.isTapped()).isTrue();
    }

    private Permanent addGoblinDirigible(Player player, boolean tapped) {
        Permanent perm = new Permanent(new GoblinDirigible());
        perm.setSummoningSick(false);
        if (tapped) {
            perm.tap();
        }
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
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
}
