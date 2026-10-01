package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.j.JodahsAvenger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagusOfTheLibrary.class, JodahsAvenger.class})
class MagusOfTheLibraryTest extends BaseCardTest {

    @Test
    @DisplayName("Adds one colorless mana")
    void addsColorlessMana() {
        addCreatureReady(player1, new MagusOfTheLibrary());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Draws a card with exactly seven cards in hand")
    void drawsWithExactlySevenCardsInHand() {
        addCreatureReady(player1, new MagusOfTheLibrary());
        harness.setHand(player1, List.of(
                new JodahsAvenger(), new JodahsAvenger(), new JodahsAvenger(), new JodahsAvenger(),
                new JodahsAvenger(), new JodahsAvenger(), new JodahsAvenger()));
        harness.setLibrary(player1, List.of(new JodahsAvenger()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(8);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot draw with fewer than seven cards in hand")
    void cannotDrawWithFewerThanSevenCardsInHand() {
        addCreatureReady(player1, new MagusOfTheLibrary());
        harness.setHand(player1, List.of(
                new JodahsAvenger(), new JodahsAvenger(), new JodahsAvenger(),
                new JodahsAvenger(), new JodahsAvenger(), new JodahsAvenger()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot draw with more than seven cards in hand")
    void cannotDrawWithMoreThanSevenCardsInHand() {
        addCreatureReady(player1, new MagusOfTheLibrary());
        harness.setHand(player1, List.of(
                new JodahsAvenger(), new JodahsAvenger(), new JodahsAvenger(), new JodahsAvenger(),
                new JodahsAvenger(), new JodahsAvenger(), new JodahsAvenger(), new JodahsAvenger()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
