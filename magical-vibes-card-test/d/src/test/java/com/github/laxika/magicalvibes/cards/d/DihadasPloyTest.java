package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DihadasPloy.class, Forest.class, Mountain.class})
class DihadasPloyTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two, discards one, and gains life for the discard")
    void drawsDiscardsAndGainsLife() {
        harness.setHand(player1, List.of(new DihadasPloy(), new Forest(), new Mountain()));
        harness.setLibrary(player1, List.of(new Forest(), new Mountain()));
        harness.setLife(player1, 10);
        addMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertLife(player1, 11);
        harness.assertInGraveyard(player1, "Dihada's Ploy");
    }

    @Test
    @DisplayName("Jump-start counts its additional discard for life gained")
    void jumpStartCountsAdditionalDiscard() {
        harness.setGraveyard(player1, List.of(new DihadasPloy()));
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Mountain()));
        harness.setLife(player1, 10);
        addMana();

        harness.castJumpStart(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 12);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Dihada's Ploy"));
    }

    @Test
    @DisplayName("Counts earlier discards in the same turn")
    void countsEarlierDiscardsThisTurn() {
        DihadasPloy ploy = new DihadasPloy();
        harness.setHand(player1, List.of(ploy, new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Mountain(), new Forest(), new Mountain()));
        harness.setLife(player1, 10);
        addMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.assertLife(player1, 11);

        addMana();
        harness.castJumpStart(player1, gd.playerGraveyards.get(player1.getId()).indexOf(ploy), 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertLife(player1, 14);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertNotInGraveyard(player1, "Dihada's Ploy");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Dihada's Ploy"));
    }

    @Test
    @DisplayName("An opponent's earlier discard does not increase life gained")
    void doesNotCountOpponentsDiscards() {
        harness.setHand(player2, List.of(new DihadasPloy(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Mountain()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        harness.setHand(player1, List.of(new DihadasPloy(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Mountain()));
        harness.setLife(player1, 10);
        addMana();
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertLife(player1, 11);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
