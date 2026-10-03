package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({ManifoldInsights.class, Forest.class, GrizzlyBears.class, Plains.class, Shock.class})
class ManifoldInsightsTest extends BaseCardTest {

    @Test
    @DisplayName("Next opponent chooses a revealed nonland card and the rest go to the bottom")
    void opponentChoosesNonlandAndRestBottomRandomly() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        List<Card> library = List.of(
                forest, bears, shock, new Plains(), new GrizzlyBears(),
                new Shock(), new Plains(), new GrizzlyBears(), new Shock(), new Plains());
        castWithLibrary(library);

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                bears.getId(), shock.getId(), library.get(4).getId(), library.get(5).getId(),
                library.get(7).getId(), library.get(8).getId());
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultipleCardsChosen(player2, List.of(shock.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(shock);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(9)
                .containsExactlyInAnyOrderElementsOf(library.stream()
                        .filter(card -> card != shock)
                        .toList());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A land is not a legal choice")
    void landCannotBeChosen() {
        Card forest = new Forest();
        List<Card> library = List.of(forest, new GrizzlyBears(), new Shock(), new Plains(),
                new Plains(), new Plains(), new Plains(), new Plains(), new Plains(), new Plains());
        castWithLibrary(library);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player2, List.of(forest.getId())))
                .hasMessageContaining("Invalid card");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class).playerId())
                .isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("With no nonland cards, all revealed cards go to the bottom")
    void noNonlandCardsNeedNoChoice() {
        List<Card> library = List.of(new Forest(), new Plains(), new Plains(), new Plains(), new Plains(),
                new Plains(), new Plains(), new Plains(), new Plains(), new Plains());
        castWithLibrary(library);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(library);
    }

    private void castWithLibrary(List<Card> library) {
        harness.setHand(player1, List.of(new ManifoldInsights()));
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
