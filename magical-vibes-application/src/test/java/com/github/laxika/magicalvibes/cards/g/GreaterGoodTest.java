package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GreaterGoodTest extends BaseCardTest {

    // ===== Draws equal to sacrificed creature's power, then discards three =====

    @Test
    @DisplayName("Sacrificing a 4-power creature draws four cards, then discards three")
    void drawsEqualToPowerThenDiscardsThree() {
        harness.addToBattlefield(player1, new GreaterGood());
        harness.addToBattlefield(player1, vanillaCreature(4, 4));
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, deck(6));

        // Only one creature on the battlefield → auto-sacrifice
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Drew four, so hand is 4 and a discard choice is now pending
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();

        // Discard the required three
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    // ===== Zero-power creature draws nothing but still discards three =====

    @Test
    @DisplayName("Sacrificing a 0-power creature draws nothing but still discards three")
    void zeroPowerDrawsNothingButStillDiscardsThree() {
        harness.addToBattlefield(player1, new GreaterGood());
        harness.addToBattlefield(player1, vanillaCreature(0, 3));
        harness.setLibrary(player1, deck(6));
        harness.setHand(player1, deck(4));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // No cards drawn: hand is still the original four
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    // ===== The sacrificed creature ends up in the graveyard =====

    @Test
    @DisplayName("The sacrificed creature is put into the graveyard")
    void sacrificedCreatureGoesToGraveyard() {
        harness.addToBattlefield(player1, new GreaterGood());
        harness.addToBattlefield(player1, vanillaCreature(2, 2));
        harness.setLibrary(player1, deck(6));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Creature gone from the battlefield, only Greater Good remains
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Bear"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Bear"));
    }

    // ===== Helpers =====

    private Card vanillaCreature(int power, int toughness) {
        Card card = new Card();
        card.setName("Bear");
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.GREEN);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    private List<Card> deck(int size) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Card card = new Card();
            card.setName("Filler " + i);
            card.setType(CardType.SORCERY);
            card.setColor(CardColor.GREEN);
            cards.add(card);
        }
        return cards;
    }
}
