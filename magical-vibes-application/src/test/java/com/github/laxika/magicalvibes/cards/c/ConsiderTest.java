package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Consider.class, Island.class})
class ConsiderTest extends BaseCardTest {

    @Test
    void surveilingTopCardIntoGraveyardDrawsNextCard() {
        Card topCard = new Island();
        Card nextCard = new Island();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        castConsider();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nextCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void leavingTopCardOnTopDrawsIt() {
        Card topCard = new Island();
        Card nextCard = new Island();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        castConsider();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.stack).isEmpty();
    }

    private void castConsider() {
        harness.setHand(player1, List.of(new Consider()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
