package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Grizzly Bears");
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

    private void addClues(int count) {
        for (int i = 0; i < count; i++) {
            harness.setHand(player1, List.of(new ThrabenInspector()));
            harness.addMana(player1, ManaColor.WHITE, 1);
            harness.castCreature(player1, 0);
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve investigate
        }
    }
}
