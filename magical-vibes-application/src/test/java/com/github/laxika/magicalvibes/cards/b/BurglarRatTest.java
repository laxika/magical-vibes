package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BurglarRat.class})
class BurglarRatTest extends BaseCardTest {

    @Test
    @DisplayName("When Burglar Rat enters, each opponent discards a card")
    void eachOpponentDiscardsACard() {
        BurglarRat discarded = new BurglarRat();
        harness.setHand(player1, new ArrayList<>(List.of(new BurglarRat())));
        harness.setHand(player2, new ArrayList<>(List.of(discarded)));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Burglar Rat");
    }

    @Test
    @DisplayName("Burglar Rat's ETB does nothing when an opponent has no cards")
    void emptyOpponentHand() {
        harness.setHand(player1, new ArrayList<>(List.of(new BurglarRat())));
        harness.setHand(player2, new ArrayList<>());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Opponent chooses exactly one card and the controller keeps their hand")
    void opponentChoosesOneCardFromLargerHand() {
        BurglarRat controllerCard = new BurglarRat();
        BurglarRat kept = new BurglarRat();
        BurglarRat discarded = new BurglarRat();
        harness.setHand(player1, List.of(new BurglarRat(), controllerCard));
        harness.setHand(player2, List.of(kept, discarded));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(controllerCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A Rat controlled by the second player makes the first player discard")
    void secondPlayersRatMakesFirstPlayerDiscard() {
        BurglarRat discarded = new BurglarRat();
        harness.setHand(player1, List.of(discarded));
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new BurglarRat(), "{1}{B}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
