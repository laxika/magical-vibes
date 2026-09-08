package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextEndStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HornOfPlentyTest extends BaseCardTest {

    private void prepareMainPhase(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("The spell's caster may pay and draws at the next end step")
    void casterPaysAndDrawsAtNextEndStep() {
        harness.addToBattlefield(player1, new HornOfPlenty());
        harness.setHand(player2, List.of(new GrizzlyBears(), new SuntailHawk()));
        harness.setLibrary(player2, List.of(new SuntailHawk()));
        harness.addMana(player2, ManaColor.GREEN, 3);
        prepareMainPhase(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        int handBeforeDraw = gd.playerHands.get(player2.getId()).size();
        assertThat(gd.getDelayedActions(DrawCardsAtNextEndStep.class))
                .singleElement()
                .extracting(DrawCardsAtNextEndStep::controllerId)
                .isEqualTo(player2.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBeforeDraw + 1);
    }

    @Test
    @DisplayName("Declining the payment does not schedule a draw")
    void decliningPaymentDoesNotScheduleDraw() {
        harness.addToBattlefield(player1, new HornOfPlenty());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        prepareMainPhase(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.getDelayedActions(DrawCardsAtNextEndStep.class)).isEmpty();
    }

    @Test
    @DisplayName("The controller is also the caster's payment recipient for their own spell")
    void controllerCastsAndPays() {
        harness.addToBattlefield(player1, new HornOfPlenty());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        prepareMainPhase(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }
}
