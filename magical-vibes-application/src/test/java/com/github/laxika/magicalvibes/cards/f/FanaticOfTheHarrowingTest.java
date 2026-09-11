package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FanaticOfTheHarrowing.class, GrizzlyBears.class, Shock.class})
class FanaticOfTheHarrowingTest extends BaseCardTest {

    @Test
    void controllerDrawsAfterDiscardingAndEachPlayerDiscards() {
        Shock controllerDiscard = new Shock();
        GrizzlyBears opponentDiscard = new GrizzlyBears();
        Shock controllerDraw = new Shock();
        harness.setHand(player1, List.of(new FanaticOfTheHarrowing(), controllerDiscard));
        harness.setHand(player2, List.of(opponentDiscard));
        harness.setLibrary(player1, List.of(controllerDraw));
        addFanaticMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(controllerDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(controllerDiscard);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentDiscard);
    }

    @Test
    void controllerDoesNotDrawWhenTheyHaveNoCardToDiscard() {
        GrizzlyBears opponentDiscard = new GrizzlyBears();
        Shock libraryCard = new Shock();
        harness.setHand(player1, List.of(new FanaticOfTheHarrowing()));
        harness.setHand(player2, List.of(opponentDiscard));
        harness.setLibrary(player1, List.of(libraryCard));
        addFanaticMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentDiscard);
    }

    @Test
    void controllerDrawsWhenOpponentHasNoCardToDiscard() {
        Shock controllerDiscard = new Shock();
        Shock controllerDraw = new Shock();
        harness.setHand(player1, List.of(new FanaticOfTheHarrowing(), controllerDiscard));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(controllerDraw));
        addFanaticMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(controllerDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(controllerDiscard);
    }

    private void addFanaticMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
