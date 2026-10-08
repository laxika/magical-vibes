package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.p.Plummet;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YouLineUpTheShot.class, Forest.class, Naturalize.class, Plummet.class})
class YouLineUpTheShotTest extends BaseCardTest {

    @Test
    void conjuresPlummet() {
        cast(0);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof Plummet);
    }

    @Test
    void conjuresNaturalize() {
        cast(1);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof Naturalize);
    }

    @Test
    void drawsACard() {
        harness.setLibrary(player1, List.of(new Forest()));
        cast(2);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof Forest);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void conjuringCreatesOnlyTheChosenCardWithoutDrawing(int modeIndex) {
        Forest libraryCard = new Forest();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player2, List.of());

        cast(modeIndex);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, modeIndex == 0 ? "Plummet" : "Naturalize");
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getOwnerId()).isEqualTo(player1.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "You Line Up the Shot");
    }

    @Test
    void warningShotDrawsExactlyTheTopCardWithoutConjuring() {
        Forest topCard = new Forest();
        Forest secondCard = new Forest();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setHand(player2, List.of());

        cast(2);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "You Line Up the Shot");
    }

    private void cast(int modeIndex) {
        harness.setHand(player1, List.of(new YouLineUpTheShot()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castModalInstant(player1, 0, modeIndex, List.of());
        harness.passBothPriorities();
    }
}
