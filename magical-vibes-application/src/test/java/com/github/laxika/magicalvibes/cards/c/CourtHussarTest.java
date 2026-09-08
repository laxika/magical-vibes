package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CourtHussar.class, GrizzlyBears.class})
class CourtHussarTest extends BaseCardTest {

    @Test
    @DisplayName("Looks at the top three cards and keeps one when white mana was spent")
    void looksAtTopThreeAndKeepsOneWhenWhiteManaWasSpent() {
        harness.setHand(player1, List.of(new CourtHussar()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        Card top = new GrizzlyBears();
        Card middle = new GrizzlyBears();
        Card bottom = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top, middle, bottom));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(middle.getId()));
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).contains(middle);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, bottom);
        harness.assertOnBattlefield(player1, "Court Hussar");
    }

    @Test
    @DisplayName("Sacrifices itself when white mana was not spent")
    void sacrificesItselfWhenWhiteManaWasNotSpent() {
        harness.setHand(player1, List.of(new CourtHussar()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setLibrary(player1, List.of());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Court Hussar");
        harness.assertInGraveyard(player1, "Court Hussar");
    }
}
