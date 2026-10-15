package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IcewindElemental.class, Shock.class, GrizzlyBears.class})
class IcewindElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield draws a card, then prompts its controller to discard")
    void entersAndLoots() {
        Card discard = new Shock();
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of(new IcewindElemental(), discard));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);

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
    @DisplayName("The controller may discard the card just drawn")
    void canDiscardNewlyDrawnCard() {
        Card kept = new IcewindElemental();
        Card drawn = new IcewindElemental();
        harness.setHand(player1, List.of(kept));
        harness.setLibrary(player1, List.of(drawn));

        harness.enterBattlefieldAndReturn(player1, new IcewindElemental());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept, drawn);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent-controlled entry loots only for that controller, even with an initially empty hand")
    void opponentControllerDrawsAndDiscardsFromEmptyHand() {
        Card unaffected = new IcewindElemental();
        Card drawn = new IcewindElemental();
        harness.setHand(player1, List.of(unaffected));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(drawn));

        harness.enterBattlefieldAndReturn(player2, new IcewindElemental());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawn);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(drawn);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(unaffected);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
