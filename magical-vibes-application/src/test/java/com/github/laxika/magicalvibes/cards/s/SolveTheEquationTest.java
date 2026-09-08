package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SolveTheEquationTest extends BaseCardTest {

    @Test
    void searchesForAnInstantOrSorcery() {
        harness.setHand(player1, List.of(new SolveTheEquation()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.getGameData().playerDecks.get(player1.getId()).clear();
        harness.getGameData().playerDecks.get(player1.getId()).addAll(List.of(
                new GrizzlyBears(), new Opt(), new Divination()));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

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
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(optIndex));

        harness.assertInHand(player1, "Opt");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    void doesNotOfferCardsOfOtherTypes() {
        harness.setHand(player1, List.of(new SolveTheEquation()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        List<Card> library = harness.getGameData().playerDecks.get(player1.getId());
        library.clear();
        library.add(new GrizzlyBears());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(library).hasSize(1).extracting(Card::getName).containsExactly("Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .doesNotContain("Grizzly Bears");
    }
}
