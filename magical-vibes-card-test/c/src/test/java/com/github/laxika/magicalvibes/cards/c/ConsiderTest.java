package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Consider.class, Island.class})
class ConsiderTest extends BaseCardTest {

    @Test
    void surveilingTopCardIntoGraveyardDrawsNextCard() {
        Card topCard = new Island();
        Card nextCard = new Island();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        castConsider();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nextCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void leavingTopCardOnTopDrawsIt() {
        Card topCard = new Island();
        Card nextCard = new Island();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        castConsider();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void drawWaitsForSurveilChoice() {
        Card topCard = new Island();
        Card nextCard = new Island();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        castConsider();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, nextCard);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
    }

    @Test
    void keepingOnlyLibraryCardDrawsItWithoutLosing() {
        Card topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));
        castConsider();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        harness.assertInGraveyard(player1, "Consider");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void surveilingOnlyLibraryCardIntoGraveyardLosesOnDraw() {
        Card topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));
        castConsider();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    void emptyLibraryStillAttemptsDrawAndLosesWithoutChoice() {
        harness.setLibrary(player1, List.of());
        castConsider();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    private void castConsider() {
        harness.setHand(player1, List.of(new Consider()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0);
    }
}
