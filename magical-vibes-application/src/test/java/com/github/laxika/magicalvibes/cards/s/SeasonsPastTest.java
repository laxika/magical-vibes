package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SeasonsPastTest extends BaseCardTest {

    @Test
    @DisplayName("Returns selected cards with different mana values and goes to the bottom of its owner's library")
    void returnsCardsWithDifferentManaValues() {
        Card bears = new GrizzlyBears();
        Card spider = new GiantSpider();
        Card elves = new LlanowarElves();
        Card holyDay = new HolyDay();
        Card seasonsPast = new SeasonsPast();
        harness.setGraveyard(player1, List.of(bears, spider, elves, holyDay));
        harness.setHand(player1, List.of(seasonsPast));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);

        harness.handleGraveyardCardChosen(player1, 2);

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice.validIndices()).containsExactly(0, 1);

        harness.handleGraveyardCardChosen(player1, 0);
        choice = gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice.validIndices()).containsExactly(0);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Giant Spider");
        harness.assertInHand(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Holy Day");
        assertThat(gd.playerDecks.get(player1.getId()).getLast().getId()).isEqualTo(seasonsPast.getId());
    }

    @Test
    @DisplayName("May return zero cards")
    void mayReturnZeroCards() {
        Card card = new GrizzlyBears();
        Card seasonsPast = new SeasonsPast();
        harness.setGraveyard(player1, List.of(card));
        harness.setHand(player1, List.of(seasonsPast));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, -1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()).getLast().getId()).isEqualTo(seasonsPast.getId());
    }
}
