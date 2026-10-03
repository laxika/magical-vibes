package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DoomedDissenter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CompellingArgument.class, DoomedDissenter.class})
class CompellingArgumentTest extends BaseCardTest {

    @Test
    @DisplayName("Mills five cards from target player's library")
    void millsFiveCards() {
        harness.setHand(player1, List.of(new CompellingArgument()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        List<Card> deck = IntStream.range(0, 10)
                .mapToObj(i -> (Card) new DoomedDissenter()).toList();
        harness.setLibrary(player2, deck);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(5);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyElementsOf(deck.subList(0, 5));
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(deck.subList(5, 10));
    }

    @Test
    @DisplayName("Mills only remaining cards when library has fewer than five")
    void millsOnlyRemainingWhenLibrarySmall() {
        harness.setHand(player1, List.of(new CompellingArgument()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setLibrary(player2, List.of(new DoomedDissenter(), new DoomedDissenter(), new DoomedDissenter()));

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new CompellingArgument()));
        harness.setLibrary(player1, List.of(new DoomedDissenter()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.assertInGraveyard(player1, "Compelling Argument");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Compelling Argument");
        harness.assertInHand(player1, "Doomed Dissenter");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can target its controller and mills exactly the top five cards")
    void canMillItsController() {
        CompellingArgument spell = new CompellingArgument();
        List<Card> library = IntStream.range(0, 6)
                .mapToObj(i -> (Card) new DoomedDissenter()).toList();
        harness.setHand(player1, List.of(spell));
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(library.get(5));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsAll(library.subList(0, 5)).contains(spell).hasSize(6);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An empty library is a legal target and milling it does not cause a loss")
    void canMillAnEmptyLibrary() {
        harness.setHand(player1, List.of(new CompellingArgument()));
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Cycling requires blue mana and leaves the card in hand if payment fails")
    void cyclingRequiresBlueMana() {
        CompellingArgument card = new CompellingArgument();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
