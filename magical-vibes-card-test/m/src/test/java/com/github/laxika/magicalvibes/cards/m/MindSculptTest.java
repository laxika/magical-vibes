package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MindSculpt.class})
class MindSculptTest extends BaseCardTest {

    @Test
    @DisplayName("Target opponent mills seven cards")
    void millsSevenCards() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new MindSculpt()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        List<Card> deck = gd.playerDecks.get(player2.getId());
        harness.setLibrary(player2, deck.subList(Math.max(0, deck.size() - 10), deck.size()));

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(7);
    }

    @Test
    @DisplayName("Mills only the remaining cards when the library is smaller than seven")
    void millsOnlyRemainingWhenLibrarySmall() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new MindSculpt()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        List<Card> deck = gd.playerDecks.get(player2.getId());
        harness.setLibrary(player2, deck.subList(Math.max(0, deck.size() - 4), deck.size()));

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetController() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new MindSculpt()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mills the top seven cards without affecting the controller's library")
    void millsTopSevenCardsOnly() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new MindSculpt()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        List<Card> library = IntStream.range(0, 10)
                .mapToObj(i -> (Card) new MindSculpt()).toList();
        harness.setLibrary(player2, library);
        List<Card> controllerLibrary = List.copyOf(gd.playerDecks.get(player1.getId()));

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactlyInAnyOrderElementsOf(library.subList(0, 7));
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(library.subList(7, 10));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(controllerLibrary);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Resolves against an opponent with an empty library")
    void resolvesWithEmptyLibrary() {
        harness.forceActivePlayer(player1);
        MindSculpt spell = new MindSculpt();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setLibrary(player2, List.of());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        assertThat(gd.stack).isEmpty();
    }
}
