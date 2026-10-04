package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextEndStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HornOfPlenty.class, FreshVolunteers.class, Forest.class, DarkRitual.class})
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
        harness.setHand(player2, List.of(new FreshVolunteers(), new FreshVolunteers()));
        harness.setLibrary(player2, List.of(new FreshVolunteers()));
        harness.addMana(player2, ManaColor.WHITE, 3);
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
        prepareMainPhase(player2);

        harness.castFromHand(player2, new FreshVolunteers(), "{1}{W}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.getDelayedActions(DrawCardsAtNextEndStep.class)).isEmpty();
    }

    @Test
    @DisplayName("The controller is also the caster's payment recipient for their own spell")
    void controllerCastsAndPays() {
        harness.addToBattlefield(player1, new HornOfPlenty());
        prepareMainPhase(player1);

        harness.castFromHand(player1, new FreshVolunteers(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("A land play does not trigger the artifact")
    void playingLandDoesNotTriggerAbility() {
        harness.addToBattlefield(player1, new HornOfPlenty());
        harness.setHand(player2, List.of(new Forest()));
        prepareMainPhase(player2);

        harness.playLand(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getDelayedActions(DrawCardsAtNextEndStep.class)).isEmpty();
    }

    @Test
    @DisplayName("A caster without mana cannot pay for the delayed draw")
    void casterWithoutManaCannotPay() {
        harness.addToBattlefield(player1, new HornOfPlenty());
        prepareMainPhase(player2);

        harness.castFromHand(player2, new FreshVolunteers(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.getDelayedActions(DrawCardsAtNextEndStep.class)).isEmpty();
    }

    @Test
    @DisplayName("The delayed ability retains Horn's controller when an opponent pays")
    void delayedAbilityRetainsOriginalController() {
        harness.addToBattlefield(player1, new HornOfPlenty());
        harness.setLibrary(player2, List.of(new FreshVolunteers()));
        prepareMainPhase(player2);
        harness.castFromHand(player2, new FreshVolunteers(), "{1}{W}");
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.stack).singleElement()
                .satisfies(entry -> assertThat(entry.getControllerId()).isEqualTo(player1.getId()));
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Removing Horn after payment does not stop the delayed draw")
    void delayedDrawSurvivesSourceRemoval() {
        harness.addToBattlefield(player1, new HornOfPlenty());
        harness.setLibrary(player2, List.of(new FreshVolunteers()));
        prepareMainPhase(player2);
        harness.castFromHand(player2, new FreshVolunteers(), "{1}{W}");
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).clear();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextEndStep.class)).isEmpty();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each paid spell schedules its own draw at the next end step")
    void multiplePaidSpellsDrawSeparately() {
        harness.addToBattlefield(player1, new HornOfPlenty());
        harness.setLibrary(player1, List.of(new FreshVolunteers(), new FreshVolunteers()));
        prepareMainPhase(player1);

        for (int i = 0; i < 2; i++) {
            harness.castFromHand(player1, new FreshVolunteers(), "{1}{W}");
            harness.addMana(player1, ManaColor.COLORLESS, 1);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        }

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Payment during an end step waits until the following end step")
    void paymentDuringEndStepWaitsForNextEndStep() {
        harness.addToBattlefield(player1, new HornOfPlenty());
        harness.setLibrary(player2, List.of(new FreshVolunteers()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new DarkRitual(), "{B}");
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getDelayedActions(DrawCardsAtNextEndStep.class)).hasSize(1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }
}
