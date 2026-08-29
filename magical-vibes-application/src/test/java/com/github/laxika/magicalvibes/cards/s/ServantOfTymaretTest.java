package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ServantOfTymaretTest extends BaseCardTest {

    @Test
    @DisplayName("Untapping Servant of Tymaret makes each opponent lose 1 life and its controller gain 1 life")
    void untapTriggerDrainsOpponent() {
        Permanent servant = harness.addToBattlefieldAndReturn(player1, new ServantOfTymaret());
        servant.tap();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        runUntapStep(player1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Servant of Tymaret's activated ability grants it a regeneration shield")
    void activatedAbilityRegeneratesServant() {
        Permanent servant = harness.addToBattlefieldAndReturn(player1, new ServantOfTymaret());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(servant.getRegenerationShield()).isEqualTo(1);
    }

    private void runUntapStep(Player untappingPlayer) {
        Player opponent = untappingPlayer.equals(player1) ? player2 : player1;
        harness.forceActivePlayer(opponent);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
