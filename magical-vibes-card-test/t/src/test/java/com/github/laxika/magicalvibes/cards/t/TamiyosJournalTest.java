package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TamiyosJournal.class, ThrabenInspector.class})
class TamiyosJournalTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger investigates, creating a Clue token")
    void upkeepTriggerCreatesClue() {
        harness.addToBattlefield(player1, new TamiyosJournal());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // UNTAP -> UPKEEP fires the trigger
        harness.passBothPriorities(); // resolve investigate

        List<Permanent> clues = findPermanents(player1, "Clue");
        assertThat(clues).hasSize(1);
        assertThat(clues.getFirst().getCard().getSubtypes()).contains(CardSubtype.CLUE);
    }

    @Test
    @DisplayName("Opponent's upkeep does not investigate")
    void opponentUpkeepDoesNotTrigger() {
        harness.addToBattlefield(player1, new TamiyosJournal());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing three Clues tutors any card to hand")
    void sacrificingThreeCluesTutors() {
        harness.addToBattlefield(player1, new TamiyosJournal());
        addClues(3);

        harness.setLibrary(player1, List.of(new ThrabenInspector()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Thraben Inspector");
        assertThat(findPermanent(player1, "Tamiyo's Journal").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate with fewer than three Clues")
    void cannotActivateWithTwoClues() {
        harness.addToBattlefield(player1, new TamiyosJournal());
        addClues(2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    @Test
    @DisplayName("Three Clues are paid before resolution even with an empty library")
    void costsArePaidBeforeEmptyLibrarySearch() {
        harness.addToBattlefield(player1, new TamiyosJournal());
        addClues(3);
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, null, null);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(findPermanent(player1, "Tamiyo's Journal").isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A tapped Journal cannot activate or sacrifice Clues")
    void tappedJournalCannotActivate() {
        harness.addToBattlefield(player1, new TamiyosJournal());
        addClues(3);
        findPermanent(player1, "Tamiyo's Journal").tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanents(player1, "Clue")).hasSize(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opponent-controlled Clues cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsClue() {
        harness.addToBattlefield(player1, new TamiyosJournal());
        addClues(3);
        Permanent opponentsClue = findPermanents(player1, "Clue").getFirst();
        gd.playerBattlefields.get(player1.getId()).remove(opponentsClue);
        gd.playerBattlefields.get(player2.getId()).add(opponentsClue);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
        assertThat(findPermanents(player2, "Clue")).hasSize(1);
        assertThat(findPermanent(player1, "Tamiyo's Journal").isTapped()).isFalse();
    }

    @Test
    @DisplayName("An investigated Clue can be sacrificed for two mana to draw")
    void investigatedClueDrawsCard() {
        harness.addToBattlefield(player1, new TamiyosJournal());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new ThrabenInspector()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int clueIndex = gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Clue"));

        harness.activateAbility(player1, clueIndex, null, null);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Thraben Inspector");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Tamiyo's Journal");
    }

    @Test
    @DisplayName("Unrestricted search must find a card and can find a noncreature")
    void unrestrictedSearchMustFindNoncreatureCard() {
        harness.addToBattlefield(player1, new TamiyosJournal());
        addClues(3);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new TamiyosJournal()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot fail to find");
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Tamiyo's Journal");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void addClues(int count) {
        for (int i = 0; i < count; i++) {
            harness.castFromHand(player1, new ThrabenInspector(), "{W}");
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve investigate
        }
    }
}
