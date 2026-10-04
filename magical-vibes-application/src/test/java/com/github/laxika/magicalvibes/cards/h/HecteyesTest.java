package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Hecteyes.class})
class HecteyesTest extends BaseCardTest {

    @Test
    @DisplayName("When Hecteyes enters, each opponent discards a card")
    void eachOpponentDiscardsACard() {
        Hecteyes discarded = new Hecteyes();
        harness.setHand(player2, List.of(discarded));
        harness.castFromHand(player1, new Hecteyes(), "{1}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Hecteyes");
    }

    @Test
    @DisplayName("Hecteyes's ETB does nothing when an opponent has no cards")
    void emptyOpponentHand() {
        harness.setHand(player2, List.of());
        harness.castFromHand(player1, new Hecteyes(), "{1}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The opponent chooses exactly one card and the controller does not discard")
    void opponentChoosesOneCardWithoutControllerDiscarding() {
        Hecteyes retained = new Hecteyes();
        Hecteyes discarded = new Hecteyes();
        Hecteyes controllerCard = new Hecteyes();
        harness.setHand(player2, List.of(retained, discarded));
        harness.castFromHand(player1, new Hecteyes(), "{1}{B}");
        harness.setHand(player1, List.of(controllerCard));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(controllerCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
