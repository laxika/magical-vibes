package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JuggernautPeddler.class, Juggernaut.class, Forest.class, GrizzlyBears.class})
class JuggernautPeddlerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles a chosen nonland card and conjures Juggernaut into that player's hand")
    void exilesNonlandAndConjuresJuggernaut() {
        Card nonland = new GrizzlyBears();
        Card land = new Forest();
        harness.setHand(player1, List.of(new JuggernautPeddler()));
        harness.setHand(player2, List.of(nonland, land));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.validIndices()).containsExactly(0);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(nonland);
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Forest", "Juggernaut");
        assertThat(gd.playerHands.get(player2.getId())).filteredOn(Card::isTokenCard)
                .extracting(Card::getName).containsExactly("Juggernaut");
    }

    @Test
    @DisplayName("Does nothing when the target has no nonland cards")
    void noNonlandCardsMeansNoChoiceOrConjuredCard() {
        Card land = new Forest();
        harness.setHand(player1, List.of(new JuggernautPeddler()));
        harness.setHand(player2, List.of(land));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("ETB can target its controller and replace their chosen nonland card")
    void canTargetController() {
        Card nonland = new GrizzlyBears();
        harness.setHand(player1, List.of(new JuggernautPeddler(), nonland));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(nonland);
        harness.assertInHand(player1, "Juggernaut");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Controller may decline to exile a revealed nonland card")
    void mayDeclineChoice() {
        Card nonland = new GrizzlyBears();
        harness.setHand(player1, List.of(new JuggernautPeddler()));
        harness.setHand(player2, List.of(nonland));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(nonland);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only nonland cards are publicly revealed")
    void doesNotRevealLands() {
        harness.setHand(player1, List.of(new JuggernautPeddler()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.gameLog).extracting(GameLogEntry::plainText)
                .anyMatch(log -> log.contains("reveals") && log.contains("Grizzly Bears"))
                .noneMatch(log -> log.contains("reveals") && log.contains("Forest"));
        harness.handleCardChosen(player1, 0);
    }

    @Test
    @DisplayName("An empty target hand causes no choice or conjure")
    void emptyHandDoesNothing() {
        harness.setHand(player1, List.of(new JuggernautPeddler()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }
}
