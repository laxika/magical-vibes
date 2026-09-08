package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RingOfMarF.class, GrizzlyBears.class})
class RingOfMarFTest extends BaseCardTest {

    @Test
    @DisplayName("Replaces the next draw with a card chosen from outside the game")
    void replacesNextDrawWithOutsideGameCard() {
        Card outsideCard = new GrizzlyBears();
        setSideboard(outsideCard);
        harness.addToBattlefield(player1, new RingOfMarF());
        harness.setHand(player1, List.of());
        Card libraryCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(libraryCard));

        activateRing();
        draw(player1);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(outsideCard);
        assertThat(search.params().sourceSideboard()).isTrue();
        assertThat(search.params().reveals()).isFalse();
        assertThat(search.params().canFailToFind()).isFalse();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(outsideCard);
        assertThat(gd.playerSideboards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    @DisplayName("An empty outside-the-game pool still replaces the draw")
    void emptyOutsideGamePoolReplacesDrawWithNothing() {
        harness.addToBattlefield(player1, new RingOfMarF());
        harness.setHand(player1, List.of());
        Card libraryCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(libraryCard));

        activateRing();
        draw(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    @DisplayName("The replacement expires at end of turn")
    void replacementExpiresAtEndOfTurn() {
        Card outsideCard = new GrizzlyBears();
        setSideboard(outsideCard);
        harness.addToBattlefield(player1, new RingOfMarF());
        harness.setHand(player1, List.of());
        Card libraryCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(libraryCard));

        activateRing();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        draw(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(outsideCard);
    }

    private void activateRing() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }

    private void setSideboard(Card... cards) {
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(cards)));
    }
}
