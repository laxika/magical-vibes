package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BruvacTheGrandiloquent;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MomentOfTruth.class, Island.class})
class MomentOfTruthTest extends BaseCardTest {

    @Test
    @DisplayName("Puts one of the top three cards into each destination")
    void distributesTopThreeCards() {
        Card topCard = new Island();
        Card handCard = new Island();
        Card bottomCard = new Island();
        harness.setLibrary(player1, List.of(topCard, handCard, bottomCard));
        harness.setHand(player1, List.of(new MomentOfTruth()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandTopBottomChoice.class);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.HandTopBottom(1, 0));

        assertThat(gd.playerHands.get(player1.getId())).contains(handCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottomCard);
    }
    @Test
    @DisplayName("Keeps unseen cards above the card placed on the bottom")
    void preservesUnseenLibraryCards() {
        Card graveyardCard = new Island();
        Card handCard = new Island();
        Card bottomCard = new Island();
        Card unseenCard = new Island();
        harness.setLibrary(player1, List.of(graveyardCard, handCard, bottomCard, unseenCard));
        harness.setHand(player1, List.of(new MomentOfTruth()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.HandTopBottom(1, 0));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unseenCard, bottomCard);
    }

    @Test
    @DisplayName("With two cards, puts one into hand and the other into the graveyard")
    void resolvesWithTwoCards() {
        Card handCard = new Island();
        Card graveyardCard = new Island();
        harness.setLibrary(player1, List.of(handCard, graveyardCard));
        harness.setHand(player1, List.of(new MomentOfTruth()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.HandTopBottom(0, 1));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("With one card, puts it into hand without drawing")
    void resolvesWithOneCard() {
        Card handCard = new Island();
        harness.setLibrary(player1, List.of(handCard));
        harness.setHand(player1, List.of(new MomentOfTruth()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(handCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Resolves with an empty library without requiring a choice")
    void resolvesWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new MomentOfTruth()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({MomentOfTruth.class, Island.class, BruvacTheGrandiloquent.class})
    @DisplayName("Putting the chosen card into the graveyard is not milling")
    void bruvacDoesNotDoubleGraveyardPlacement() {
        Card graveyardCard = new Island();
        Card handCard = new Island();
        Card bottomCard = new Island();
        Card unseenCard = new Island();
        harness.addToBattlefield(player2, new BruvacTheGrandiloquent());
        harness.setLibrary(player1, List.of(graveyardCard, handCard, bottomCard, unseenCard));
        harness.setHand(player1, List.of(new MomentOfTruth()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.HandTopBottom(1, 0));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard).doesNotContain(unseenCard, bottomCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unseenCard, bottomCard);
    }
}
