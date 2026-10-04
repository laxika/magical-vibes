package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FieldResearch.class})
class FieldResearchTest extends BaseCardTest {

    @Test
    void drawsTwoCardsWithoutKicker() {
        harness.setLibrary(player1, List.of(new FieldResearch(), new FieldResearch(), new FieldResearch()));
        harness.castFromHand(player1, new FieldResearch(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void drawsThreeCardsWithKicker() {
        harness.setHand(player1, List.of(new FieldResearch()));
        harness.setLibrary(player1, List.of(new FieldResearch(), new FieldResearch(), new FieldResearch()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castKickedSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    void drawsOnlyTwoWhenKickerIsNotChosenDespiteSufficientMana() {
        harness.setLibrary(player1, List.of(new FieldResearch(), new FieldResearch(), new FieldResearch()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFromHand(player1, new FieldResearch(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void cannotKickWithOnlyTheBaseManaCost() {
        harness.setHand(player1, List.of(new FieldResearch()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castKickedSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotKickWithoutTheSecondBlueMana() {
        harness.setHand(player1, List.of(new FieldResearch()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castKickedSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
