package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DormantGomazoaTest extends BaseCardTest {

    @Test
    void entersTapped() {
        harness.setHand(player1, List.of(new DormantGomazoa()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Dormant Gomazoa").isTapped()).isTrue();
    }

    @Test
    void doesNotUntapDuringControllerUntapStep() {
        Permanent gomazoa = harness.addToBattlefieldAndReturn(player1, new DormantGomazoa());
        gomazoa.tap();

        advanceToNextTurn(player2);

        assertThat(gomazoa.isTapped()).isTrue();
    }

    @Test
    void controllerBecomingTargetOfOpponentSpellMayLeaveItTapped() {
        Permanent gomazoa = harness.addToBattlefieldAndReturn(player1, new DormantGomazoa());
        gomazoa.tap();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gomazoa.isTapped()).isTrue();
    }

    @Test
    void controllerBecomingTargetOfOwnSpellMayUntapIt() {
        Permanent gomazoa = harness.addToBattlefieldAndReturn(player1, new DormantGomazoa());
        gomazoa.tap();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gomazoa.isTapped()).isFalse();
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
