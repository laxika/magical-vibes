package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GatewayPlaza;
import com.github.laxika.magicalvibes.cards.g.GruulGuildgate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShimmerOfPossibility.class, SenateCourier.class, SummaryJudgment.class, GatewayPlaza.class, GruulGuildgate.class})
class ShimmerOfPossibilityTest extends BaseCardTest {

    @Test
    @DisplayName("Looks at the top four, puts one into hand, and randomizes the rest onto the bottom")
    void choosesOneAndRandomizesTheRest() {
        Card chosen = new SenateCourier();
        Card second = new SummaryJudgment();
        Card third = new GatewayPlaza();
        Card fourth = new GruulGuildgate();
        Card untouched = new SenateCourier();
        harness.setLibrary(player1, List.of(chosen, second, third, fourth, untouched));
        harness.setHand(player1, List.of(new ShimmerOfPossibility()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.randomRemainingToBottom()).isTrue();
        assertThat(choice.reorderRemainingToBottom()).isFalse();
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.allCards()).containsExactly(chosen, second, third, fourth);

        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 4))
                .containsExactlyInAnyOrder(second, third, fourth);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Shimmer of Possibility");
    }
    @Test
    void choosesFromAllCardsWhenLibraryHasFewerThanFour() {
        Card first = new SenateCourier();
        Card chosen = new GatewayPlaza();
        Card last = new SummaryJudgment();
        harness.setLibrary(player1, List.of(first, chosen, last));
        harness.setHand(player1, List.of(new ShimmerOfPossibility()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, last);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Shimmer of Possibility");
    }

    @Test
    void putsOnlyCardIntoHandWithoutAChoice() {
        Card onlyCard = new SummaryJudgment();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.setHand(player1, List.of(new ShimmerOfPossibility()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Shimmer of Possibility");
    }

    @Test
    void resolvesWithAnEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new ShimmerOfPossibility()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Shimmer of Possibility");
    }

    @Test
    void requiresExactlyOneOfTheLookedAtCards() {
        Card first = new SenateCourier();
        Card second = new SummaryJudgment();
        Card third = new GatewayPlaza();
        Card fourth = new GruulGuildgate();
        Card untouched = new SenateCourier();
        harness.setLibrary(player1, List.of(first, second, third, fourth, untouched));
        harness.setHand(player1, List.of(new ShimmerOfPossibility()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(untouched.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player2, List.of(first.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(fourth.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(fourth);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 4))
                .containsExactlyInAnyOrder(first, second, third);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Shimmer of Possibility");
    }
}
