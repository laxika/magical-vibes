package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoonOfTheWishGiver.class})
class BoonOfTheWishGiverTest extends BaseCardTest {

    @Test
    @DisplayName("Draws four cards")
    void drawsFourCards() {
        harness.setHand(player1, List.of(new BoonOfTheWishGiver()));
        harness.setLibrary(player1, List.of(
                new BoonOfTheWishGiver(),
                new BoonOfTheWishGiver(),
                new BoonOfTheWishGiver(),
                new BoonOfTheWishGiver()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        harness.assertInHand(player1, "Boon of the Wish-Giver");
    }

    @Test
    @DisplayName("Cycling discards Boon of the Wish-Giver and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new BoonOfTheWishGiver()));
        harness.setLibrary(player1, List.of(new BoonOfTheWishGiver()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Boon of the Wish-Giver");
        harness.assertInHand(player1, "Boon of the Wish-Giver");
    }

    @Test
    @DisplayName("Cycling discards immediately but draws only when the ability resolves")
    void cyclingPaysDiscardBeforeDrawing() {
        BoonOfTheWishGiver cycledCard = new BoonOfTheWishGiver();
        BoonOfTheWishGiver drawnCard = new BoonOfTheWishGiver();
        harness.setHand(player1, List.of(cycledCard));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(cycledCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cycling cannot be activated without paying one mana")
    void cyclingRequiresMana() {
        BoonOfTheWishGiver card = new BoonOfTheWishGiver();
        harness.setHand(player1, List.of(card));

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        harness.assertNotInGraveyard(player1, "Boon of the Wish-Giver");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling can be activated during an opponent's turn with colored mana")
    void cyclingOnOpponentsTurn() {
        BoonOfTheWishGiver drawnCard = new BoonOfTheWishGiver();
        harness.setHand(player1, List.of(new BoonOfTheWishGiver()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.assertInGraveyard(player1, "Boon of the Wish-Giver");
        assertThat(gd.stack).isEmpty();
    }
}
