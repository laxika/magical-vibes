package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HondenOfSeeingWindsTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card for each Shrine its controller controls")
    void drawsForEachControlledShrine() {
        harness.addToBattlefield(player1, new HondenOfSeeingWinds());
        harness.addToBattlefield(player1, shrine());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("Counts Shrines when the upkeep trigger resolves")
    void recountsShrinesAtResolution() {
        harness.addToBattlefield(player1, new HondenOfSeeingWinds());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.addToBattlefield(player1, shrine());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new HondenOfSeeingWinds());
        harness.addToBattlefield(player2, shrine());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    private Card shrine() {
        Card card = new Card();
        card.setName("Test Shrine");
        card.setType(CardType.ENCHANTMENT);
        card.setSubtypes(List.of(CardSubtype.SHRINE));
        return card;
    }
}
