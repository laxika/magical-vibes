package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SolveTheEquation.class, GrizzlyBears.class, Opt.class, Divination.class})
class SolveTheEquationTest extends BaseCardTest {

    @Test
    void searchesForAnInstantOrSorcery() {
        harness.setHand(player1, List.of(new SolveTheEquation()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new Opt(), new Divination()));

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactlyInAnyOrder("Opt", "Divination");
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();

        int optIndex = search.params().cards().stream()
                .map(Card::getName)
                .toList()
                .indexOf("Opt");
        harness.handleCardChosen(player1, optIndex);

        harness.assertInHand(player1, "Opt");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    void doesNotOfferCardsOfOtherTypes() {
        harness.setHand(player1, List.of(new SolveTheEquation()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        List<Card> library = harness.getGameData().playerDecks.get(player1.getId());

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(library).hasSize(1).extracting(Card::getName).containsExactly("Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .doesNotContain("Grizzly Bears");
    }

    @Test
    void putsOnlyTheChosenSorceryIntoHand() {
        SolveTheEquation chosen = new SolveTheEquation();
        SolveTheEquation remaining = new SolveTheEquation();
        harness.setHand(player1, List.of(new SolveTheEquation()));
        harness.setLibrary(player1, List.of(chosen, remaining));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new SolveTheEquation()));
        List<Card> opponentLibrary = List.copyOf(gd.playerDecks.get(player2.getId()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(opponentLibrary);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Solve the Equation");
    }

    @Test
    void mayFailToFindEvenWhenASorceryIsPresent() {
        SolveTheEquation available = new SolveTheEquation();
        harness.setHand(player1, List.of(new SolveTheEquation()));
        harness.setLibrary(player1, List.of(available));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(available);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Solve the Equation");
    }

    @Test
    void resolvesWithAnEmptyLibrary() {
        harness.setHand(player1, List.of(new SolveTheEquation()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Solve the Equation");
    }
}
