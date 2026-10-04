package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Harmonize.class, HealingLeaves.class})
class HarmonizeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Harmonize draws three cards")
    void castingDrawsThreeCards() {
        HealingLeaves first = new HealingLeaves();
        HealingLeaves second = new HealingLeaves();
        HealingLeaves third = new HealingLeaves();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new Harmonize()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second, third);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Harmonize draws only the top three cards and leaves the opponent's zones unchanged")
    void drawsOnlyThreeCardsForController() {
        HealingLeaves first = new HealingLeaves();
        HealingLeaves second = new HealingLeaves();
        HealingLeaves third = new HealingLeaves();
        HealingLeaves fourth = new HealingLeaves();
        HealingLeaves opponentCard = new HealingLeaves();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.setLibrary(player2, List.of(opponentCard));
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new Harmonize()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second, third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
        harness.assertInGraveyard(player1, "Harmonize");
    }

    @Test
    @DisplayName("Drawing exactly the last three cards does not cause a loss")
    void drawsLastThreeCardsWithoutLosing() {
        harness.setLibrary(player1, List.of(new HealingLeaves(), new HealingLeaves(), new HealingLeaves()));
        harness.setHand(player1, List.of(new Harmonize()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("With fewer than three cards, Harmonize draws the remaining cards and its controller loses")
    void shortLibraryCausesLossAfterDrawingRemainingCards() {
        HealingLeaves first = new HealingLeaves();
        HealingLeaves second = new HealingLeaves();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new Harmonize()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Resolving Harmonize with an empty library causes its controller to lose")
    void emptyLibraryCausesLoss() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new Harmonize()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }
}
