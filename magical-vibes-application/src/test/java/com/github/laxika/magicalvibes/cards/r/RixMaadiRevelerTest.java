package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BurningTreeVandal;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Scorchmark;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RixMaadiReveler.class, BurningTreeVandal.class, Island.class, Scorchmark.class})
class RixMaadiRevelerTest extends BaseCardTest {

    @Test
    @DisplayName("Normally discards a card, then draws a card when it enters")
    void normallyDiscardsThenDraws() {
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(
                new RixMaadiReveler(), new BurningTreeVandal(), new Scorchmark()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Burning-Tree Vandal");
        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Scorchmark", "Island");
    }

    @Test
    @DisplayName("For spectacle, discards the hand and draws three cards")
    void spectacleDiscardsHandThenDrawsThree() {
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.setHand(player1, List.of(
                new RixMaadiReveler(), new BurningTreeVandal(), new Scorchmark()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Island", "Island", "Island");
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Burning-Tree Vandal", "Scorchmark");
    }

    @Test
    @DisplayName("Spectacle cannot be used unless an opponent lost life this turn")
    void spectacleRequiresOpponentLifeLoss() {
        harness.setHand(player1, List.of(new RixMaadiReveler()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Normal-cost entry draws even when there is no card to discard")
    void normalCostDrawsWithEmptyHand() {
        Island drawnCard = new Island();
        harness.setLibrary(player1, List.of(drawnCard, new Island()));
        harness.castFromHand(player1, new RixMaadiReveler(), "{1}{R}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Spectacle draws three even when the hand is empty")
    void spectacleDrawsWithEmptyHand() {
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        List<Island> drawnCards = List.of(new Island(), new Island(), new Island());
        harness.setLibrary(player1, drawnCards);
        harness.setHand(player1, List.of(new RixMaadiReveler()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(drawnCards);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The controller's life loss alone does not enable spectacle")
    void ownLifeLossDoesNotEnableSpectacle() {
        gd.lifeLostThisTurn.put(player1.getId(), 1);
        harness.setHand(player1, List.of(new RixMaadiReveler()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Paying the normal cost uses the normal ability even when spectacle is available")
    void normalCostStillDiscardsOneWhenSpectacleIsAvailable() {
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        Island drawnCard = new Island();
        BurningTreeVandal discardedCard = new BurningTreeVandal();
        Scorchmark retainedCard = new Scorchmark();
        harness.setLibrary(player1, List.of(drawnCard, new Island(), new Island()));
        harness.setHand(player1, List.of(new RixMaadiReveler(), discardedCard, retainedCard));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retainedCard, drawnCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discardedCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The normal draw completes before an opponent can respond after the discard")
    void discardAndDrawResolveTogether() {
        Island drawnCard = new Island();
        BurningTreeVandal discardedCard = new BurningTreeVandal();
        harness.setLibrary(player1, List.of(drawnCard, new Island()));
        harness.setHand(player1, List.of(new RixMaadiReveler(), discardedCard));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player2, List.of(new Scorchmark()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discardedCard);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.stack).isEmpty();
    }
}
