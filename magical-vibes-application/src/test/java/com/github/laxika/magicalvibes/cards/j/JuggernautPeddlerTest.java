package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
    @DisplayName("ETB can target only an opponent")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new JuggernautPeddler()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }
}
