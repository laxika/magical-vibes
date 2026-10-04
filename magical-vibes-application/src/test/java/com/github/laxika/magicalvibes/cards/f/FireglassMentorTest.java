package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.currentStep).isEqualTo(TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    private void advanceToPostcombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }

    @Test
    void doesNotTriggerDuringOpponentsMainPhase() {
        harness.addToBattlefield(player1, new FireglassMentor());
        gd.lifeLostThisTurn.put(player2.getId(), 1);

        advanceToPostcombatMain(player2);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void emptyLibraryDoesNotRequireAChoice() {
        harness.addToBattlefield(player1, new FireglassMentor());
        harness.setLibrary(player1, List.of());
        gd.lifeLostThisTurn.put(player2.getId(), 1);

        advanceToPostcombatMain(player1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void singleRemainingCardCanBeChosenAndPlayedAsALand() {
        harness.addToBattlefield(player1, new FireglassMentor());
        var land = new Island();
        harness.setLibrary(player1, List.of(land));
        gd.lifeLostThisTurn.put(player2.getId(), 1);

        advanceToPostcombatMain(player1);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));
        harness.castFromExile(player1, land.getId());

        harness.assertOnBattlefield(player1, "Island");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void chosenSpellRequiresItsManaCostAndOnlyChosenCardCanBePlayed() {
        harness.addToBattlefield(player1, new FireglassMentor());
        var chosen = new FireglassMentor();
        var other = new Island();
        harness.setLibrary(player1, List.of(chosen, other));
        gd.lifeLostThisTurn.put(player2.getId(), 1);

        advanceToPostcombatMain(player1);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThatThrownBy(() -> harness.castFromExile(player1, chosen.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castFromExile(player1, other.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, chosen.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> assertThat(permanent.getCard().getId()).isEqualTo(chosen.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(other);
    }

    @Test
    void unusedPermissionExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new FireglassMentor());
        var chosen = new Island();
        harness.setLibrary(player1, List.of(chosen));
        gd.lifeLostThisTurn.put(player2.getId(), 1);

        advanceToPostcombatMain(player1);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(chosen);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(chosen.getId());
        assertThatThrownBy(() -> harness.castFromExile(player1, chosen.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void lifeGainDoesNotUndoEarlierLifeLoss() {
        harness.addToBattlefield(player1, new FireglassMentor());
        var chosen = new Island();
        harness.setLibrary(player1, List.of(chosen));
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        harness.setLife(player2, 21);

        advanceToPostcombatMain(player1);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(chosen);
        assertThat(gd.exilePlayPermissions).containsEntry(chosen.getId(), player1.getId());
    }

    @Test
    void choosingACardIsMandatoryEvenWhenItWillNotBePlayed() {
        harness.addToBattlefield(player1, new FireglassMentor());
        var chosen = new Island();
        harness.setLibrary(player1, List.of(chosen));
        gd.lifeLostThisTurn.put(player2.getId(), 1);

        advanceToPostcombatMain(player1);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(chosen);
    }

    @Test
    void thirdMainPhaseDoesNotTriggerAgain() {
        harness.addToBattlefield(player1, new FireglassMentor());
        var first = new Island();
        var second = new Island();
        var remainder = new Island();
        harness.setLibrary(player1, List.of(first, second, remainder));
        gd.lifeLostThisTurn.put(player2.getId(), 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        gd.additionalCombatMainPhasePairs = 1;
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainder);
    }
}
