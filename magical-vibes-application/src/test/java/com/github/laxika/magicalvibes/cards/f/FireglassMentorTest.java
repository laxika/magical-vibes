package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FireglassMentor.class, Island.class, Shock.class})
class FireglassMentorTest extends BaseCardTest {

    @Test
    @DisplayName("Postcombat main trigger exiles the top two cards and lets the controller play one")
    void exilesTopTwoAndGrantsPlayPermissionToChosenCard() {
        harness.addToBattlefield(player1, new FireglassMentor());
        var chosen = new Island();
        var other = new Shock();
        harness.setLibrary(player1, List.of(chosen, other));
        gd.lifeLostThisTurn.put(player2.getId(), 1);

        advanceToPostcombatMain(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(chosen, other);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExiledCardMayPlayChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.exilePlayPermissions).containsEntry(chosen.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(chosen.getId());
        assertThat(gd.exilePlayPermissions).doesNotContainKey(other.getId());
    }

    @Test
    @DisplayName("Does not trigger when no opponent lost life this turn")
    void doesNotTriggerWithoutOpponentLifeLoss() {
        harness.addToBattlefield(player1, new FireglassMentor());
        harness.setLibrary(player1, List.of(new Island(), new Shock()));

        advanceToPostcombatMain(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger from the controller losing life")
    void doesNotTriggerFromControllerLifeLoss() {
        harness.addToBattlefield(player1, new FireglassMentor());
        harness.setLibrary(player1, List.of(new Island(), new Shock()));
        gd.lifeLostThisTurn.put(player1.getId(), 1);

        advanceToPostcombatMain(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Triggers only in the postcombat main phase")
    void doesNotTriggerInPrecombatMain() {
        harness.addToBattlefield(player1, new FireglassMentor());
        harness.setLibrary(player1, List.of(new Island(), new Shock()));
        gd.lifeLostThisTurn.put(player2.getId(), 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    private void advanceToPostcombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }
}
