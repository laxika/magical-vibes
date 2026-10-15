package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.k.KitsaOtterballElite;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PearlOfWisdom.class, KitsaOtterballElite.class, Forest.class})
class PearlOfWisdomTest extends BaseCardTest {

    @Test
    void costsOneLessWithOtterAndDrawsTwoCards() {
        harness.addToBattlefield(player1, new KitsaOtterballElite());
        Forest firstDraw = new Forest();
        Forest secondDraw = new Forest();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new PearlOfWisdom()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }

    @Test
    void doesNotGetCostReductionWithoutOtter() {
        harness.setHand(player1, List.of(new PearlOfWisdom()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void drawsTwoCardsAtFullCostWithoutOtter() {
        Forest firstDraw = new Forest();
        Forest secondDraw = new Forest();
        Forest remainingCard = new Forest();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, remainingCard));
        harness.setHand(player1, List.of(new PearlOfWisdom()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        harness.assertInGraveyard(player1, "Pearl of Wisdom");
    }

    @Test
    void opponentsOtterDoesNotReduceCost() {
        harness.addToBattlefield(player2, new KitsaOtterballElite());
        harness.setHand(player1, List.of(new PearlOfWisdom()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void otterDoesNotReduceBlueManaRequirement() {
        harness.addToBattlefield(player1, new KitsaOtterballElite());
        harness.setHand(player1, List.of(new PearlOfWisdom()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
