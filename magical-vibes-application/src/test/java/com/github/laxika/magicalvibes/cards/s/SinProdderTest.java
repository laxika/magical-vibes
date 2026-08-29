package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SinProdderTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent declines and the revealed card goes to hand")
    void opponentDeclinesPutsCardIntoHand() {
        Card bears = new GrizzlyBears();
        prepareUpkeep(bears);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertLife(player2, 20);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Opponent accepts and is dealt the revealed card's mana value")
    void opponentAcceptsDealsManaValueDamageAndPutsCardInGraveyard() {
        Card bears = new GrizzlyBears();
        prepareUpkeep(bears);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An empty library does not create an opponent choice")
    void emptyLibraryDoesNothing() {
        harness.addToBattlefield(player1, new SinProdder());
        gd.playerDecks.get(player1.getId()).clear();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertLife(player2, 20);
    }

    private void prepareUpkeep(Card topCard) {
        harness.addToBattlefield(player1, new SinProdder());
        gd.playerDecks.get(player1.getId()).addFirst(topCard);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
    }
}
