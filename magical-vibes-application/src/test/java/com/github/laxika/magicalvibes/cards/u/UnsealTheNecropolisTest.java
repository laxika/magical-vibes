package com.github.laxika.magicalvibes.cards.u;

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

@CardUsed({UnsealTheNecropolis.class, GrizzlyBears.class, Forest.class})
class UnsealTheNecropolisTest extends BaseCardTest {

    @Test
    @DisplayName("Each player mills three cards, then up to two creature cards return to hand")
    void millsEachPlayerAndReturnsUpToTwoCreatures() {
        Card firstCreature = new GrizzlyBears();
        Card secondCreature = new GrizzlyBears();
        Card thirdCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstCreature, secondCreature));
        harness.setLibrary(player1, List.of(thirdCreature, new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        Card spell = new UnsealTheNecropolis();
        harness.setHand(player1, List.of(spell));
        addMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);

        harness.handleGraveyardCardChosen(player1, indexOfCard(player1, firstCreature));
        harness.handleGraveyardCardChosen(player1, indexOfCard(player1, secondCreature));

        assertThat(gd.playerHands.get(player1.getId())).contains(firstCreature, secondCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(thirdCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card == firstCreature || card == secondCreature);
    }

    @Test
    @DisplayName("Only creature cards are returned and fewer than two may be chosen")
    void filtersNoncreaturesAndReturnsFewerThanTwo() {
        Card creature = new GrizzlyBears();
        Card noncreature = new Forest();
        harness.setGraveyard(player1, List.of(creature, noncreature));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        Card spell = new UnsealTheNecropolis();
        harness.setHand(player1, List.of(spell));
        addMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(noncreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private int indexOfCard(com.github.laxika.magicalvibes.model.Player player, Card card) {
        List<Card> graveyard = gd.playerGraveyards.get(player.getId());
        for (int i = 0; i < graveyard.size(); i++) {
            if (graveyard.get(i).getId().equals(card.getId())) {
                return i;
            }
        }
        throw new AssertionError("Card not found in graveyard: " + card.getId());
    }
}
