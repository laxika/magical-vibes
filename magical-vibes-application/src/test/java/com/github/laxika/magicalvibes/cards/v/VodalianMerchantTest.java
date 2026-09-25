package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VodalianMerchant.class, Island.class, Forest.class})
class VodalianMerchantTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield draws a card, then prompts its controller to discard")
    void entersAndLoots() {
        Card discard = new Island();
        Card drawn = new Forest();
        harness.setHand(player1, List.of(new VodalianMerchant(), discard));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discard, drawn);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("With no other card in hand, the drawn card is the one discarded")
    void drawsThenDiscardsWhenHandStartsEmpty() {
        Card drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.castFromHand(player1, new VodalianMerchant(), "{1}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawn);
        assertThat(gd.stack).isEmpty();
    }
}
