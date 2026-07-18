package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MaralenOfTheMornsongTest extends BaseCardTest {

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2; // avoid the starting player's first-turn draw skip
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances from UPKEEP to DRAW
    }

    @Test
    @DisplayName("The normal draw-step draw is prevented for the active player")
    void normalDrawIsPrevented() {
        harness.addToBattlefield(player1, new MaralenOfTheMornsong());

        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);

        advanceToDraw(player1);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("Active player loses 3 life and searches their library for a card to hand")
    void activePlayerLosesLifeAndTutors() {
        harness.addToBattlefield(player1, new MaralenOfTheMornsong());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Plains(), new Swamp(), new GrizzlyBears()));
        int lifeBefore = gd.getLife(player1.getId());

        advanceToDraw(player1);
        harness.passBothPriorities(); // resolve the draw-step trigger

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 3);

        var search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().playerId()).isEqualTo(player1.getId());
        assertThat(search.params().canFailToFind()).isFalse();
        assertThat(search.params().cards()).hasSize(3);

        String chosen = search.params().cards().getFirst().getName();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals(chosen));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Trigger fires on an opponent's draw step and acts on that player")
    void triggersOnOpponentDrawStep() {
        harness.addToBattlefield(player1, new MaralenOfTheMornsong());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Plains(), new Swamp()));
        int lifeBefore = gd.getLife(player2.getId());

        advanceToDraw(player2);
        harness.passBothPriorities(); // resolve the draw-step trigger

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);

        var search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().playerId()).isEqualTo(player2.getId());

        harness.getGameService().handleInteractionAnswer(gd, player2, new InteractionAnswer.LibraryCardChosen(0));
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Life is still lost even when the library is empty (search does nothing)")
    void emptyLibraryStillLosesLife() {
        harness.addToBattlefield(player1, new MaralenOfTheMornsong());
        gd.playerDecks.get(player1.getId()).clear();
        int lifeBefore = gd.getLife(player1.getId());

        advanceToDraw(player1);
        harness.passBothPriorities(); // resolve the draw-step trigger

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }
}
