package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LongRest.class})
class LongRestTest extends BaseCardTest {

    @Test
    void returnsEightDifferentManaValuesAndResetsLife() {
        List<Card> cards = List.of(
                graveyardCard("MV 0", 0), graveyardCard("MV 1", 1),
                graveyardCard("MV 2", 2), graveyardCard("MV 3", 3),
                graveyardCard("MV 4", 4), graveyardCard("MV 5", 5),
                graveyardCard("MV 6", 6), graveyardCard("MV 7", 7));
        LongRest longRest = new LongRest();
        harness.setGraveyard(player1, cards);
        harness.setHand(player1, List.of(longRest));
        harness.setLife(player1, 5);
        harness.addMana(player1, ManaColor.GREEN, 11);

        harness.castSorcery(player1, 0, 8);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrderElementsOf(
                cards.stream().map(Card::getId).toList());
        harness.handleMultipleCardsChosen(player1, cards.stream().map(Card::getId).toList());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(GameData.STARTING_LIFE_TOTAL);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrderElementsOf(cards);
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card() == longRest);
    }

    @Test
    void duplicateManaValuesCannotBeChosen() {
        Card first = graveyardCard("MV 1A", 1);
        Card duplicate = graveyardCard("MV 1B", 1);
        Card different = graveyardCard("MV 2", 2);
        harness.setGraveyard(player1, List.of(first, duplicate, different));
        harness.setHand(player1, List.of(new LongRest()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorcery(player1, 0, 2);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(first.getId(), duplicate.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different mana values");
    }

    @Test
    void fewerThanEightReturnedCardsDoNotResetLife() {
        List<Card> cards = List.of(
                graveyardCard("MV 0", 0), graveyardCard("MV 1", 1),
                graveyardCard("MV 2", 2), graveyardCard("MV 3", 3),
                graveyardCard("MV 4", 4), graveyardCard("MV 5", 5),
                graveyardCard("MV 6", 6));
        harness.setGraveyard(player1, cards);
        harness.setHand(player1, List.of(new LongRest()));
        harness.setLife(player1, 5);
        harness.addMana(player1, ManaColor.GREEN, 10);

        harness.castSorcery(player1, 0, 7);
        harness.handleMultipleCardsChosen(player1, cards.stream().map(Card::getId).toList());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(5);
    }

    @Test
    void cannotCastForMoreDistinctManaValuesThanAvailable() {
        Card first = graveyardCard("MV 1A", 1);
        Card duplicate = graveyardCard("MV 1B", 1);
        harness.setGraveyard(player1, List.of(first, duplicate));
        harness.setHand(player1, List.of(new LongRest()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2))
                .isInstanceOf(IllegalStateException.class);
    }

    private Card graveyardCard(String name, int manaValue) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{" + manaValue + "}");
        return card;
    }
}
