package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WarchanterOfMogisTest extends BaseCardTest {

    @Test
    @DisplayName("Untapping Warchanter of Mogis queues a target creature choice")
    void untappingQueuesTargetChoice() {
        Permanent warchanter = harness.addToBattlefieldAndReturn(player1, new WarchanterOfMogis());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        warchanter.tap();

        runUntapStep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.SelfTriggeredAbilityTarget.class);
        assertThat(((PendingInteraction.PermanentChoice) gd.interaction.activeInteraction()).validIds())
                .contains(bears.getId());
    }

    @Test
    @DisplayName("The chosen creature you control gains intimidate until end of turn")
    void grantsIntimidateToChosenCreature() {
        Permanent warchanter = harness.addToBattlefieldAndReturn(player1, new WarchanterOfMogis());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        warchanter.tap();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        runUntapStep(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INTIMIDATE)).isTrue();
    }

    @Test
    @DisplayName("Intimidate wears off at end of turn")
    void intimidateWearsOffAtEndOfTurn() {
        Permanent warchanter = harness.addToBattlefieldAndReturn(player1, new WarchanterOfMogis());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        warchanter.tap();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        runUntapStep(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        gd.interaction.clearAwaitingInput();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INTIMIDATE)).isFalse();
    }

    @Test
    @DisplayName("The trigger cannot target a creature controlled by an opponent")
    void cannotTargetOpponentsCreature() {
        Permanent warchanter = harness.addToBattlefieldAndReturn(player1, new WarchanterOfMogis());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        warchanter.tap();

        runUntapStep(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, ownBears.getId());
        harness.passBothPriorities();
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
