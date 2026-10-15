package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrushingDisappointment.class})
class CrushingDisappointmentTest extends BaseCardTest {

    @Test
    @DisplayName("Each player loses 2 life, then the caster draws two cards")
    void eachPlayerLosesLifeAndCasterDraws() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new CrushingDisappointment()));
        harness.setLibrary(player1, List.of(new CrushingDisappointment(), new CrushingDisappointment()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Crushing Disappointment");
    }

    @Test
    @DisplayName("The nonactive caster draws exactly two cards from their own library")
    void nonactiveCasterDrawsFromOwnLibrary() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new CrushingDisappointment()));
        CrushingDisappointment first = new CrushingDisappointment();
        CrushingDisappointment second = new CrushingDisappointment();
        CrushingDisappointment third = new CrushingDisappointment();
        harness.setLibrary(player2, List.of(first, second, third));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player2, 0);

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(third);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Crushing Disappointment");
    }

    @Test
    @DisplayName("The caster still draws before losing the game at zero life")
    void drawsBeforeLethalLifeLossEndsGame() {
        harness.setLife(player1, 2);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CrushingDisappointment()));
        CrushingDisappointment first = new CrushingDisappointment();
        CrushingDisappointment second = new CrushingDisappointment();
        harness.setLibrary(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0);

        harness.assertLife(player1, 0);
        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }
}
