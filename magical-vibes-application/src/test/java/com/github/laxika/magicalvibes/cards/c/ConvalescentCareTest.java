package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConvalescentCare.class, GrizzlyBears.class})
class ConvalescentCareTest extends BaseCardTest {

    @Test
    @DisplayName("At 5 or less life, the upkeep trigger gains 3 life and draws a card")
    void lowLifeGainsLifeAndDraws() {
        Card libraryCard = new GrizzlyBears();
        harness.addToBattlefield(player1, new ConvalescentCare());
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setLife(player1, 5);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(8);
        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(handSizeBefore + 1)
                .contains(libraryCard);
    }

    @Test
    @DisplayName("Above 5 life, the upkeep trigger does nothing")
    void aboveThresholdDoesNothing() {
        Card libraryCard = new GrizzlyBears();
        harness.addToBattlefield(player1, new ConvalescentCare());
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setLife(player1, 6);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(6);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore).doesNotContain(libraryCard);
    }

    @Test
    @DisplayName("The trigger fires only during its controller's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        Card libraryCard = new GrizzlyBears();
        harness.addToBattlefield(player1, new ConvalescentCare());
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setLife(player1, 5);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(5);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore).doesNotContain(libraryCard);
    }
}
