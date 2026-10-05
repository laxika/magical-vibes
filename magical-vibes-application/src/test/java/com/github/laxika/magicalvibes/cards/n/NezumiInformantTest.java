package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NezumiInformant.class, GrizzlyBears.class})
class NezumiInformantTest extends BaseCardTest {

    @Test
    @DisplayName("When Nezumi Informant enters, each opponent discards a card")
    void eachOpponentDiscardsACard() {
        GrizzlyBears discarded = new GrizzlyBears();
        harness.setHand(player2, List.of(discarded));

        harness.castFromHand(player1, new NezumiInformant(), "{1}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Nezumi Informant's ETB does nothing when an opponent has no cards")
    void emptyOpponentHand() {
        harness.setHand(player2, List.of());

        harness.castFromHand(player1, new NezumiInformant(), "{1}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The opponent chooses exactly one card and the controller keeps their hand")
    void opponentChoosesOneCard() {
        NezumiInformant kept = new NezumiInformant();
        NezumiInformant discarded = new NezumiInformant();
        NezumiInformant controllerCard = new NezumiInformant();
        harness.setHand(player1, List.of(controllerCard));
        harness.setHand(player2, List.of(kept, discarded));

        harness.enterBattlefieldAndReturn(player1, new NezumiInformant());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(controllerCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An Informant entering under the other player's control makes its opponent discard")
    void otherControllerMakesActivePlayerDiscard() {
        NezumiInformant discarded = new NezumiInformant();
        NezumiInformant kept = new NezumiInformant();
        harness.setHand(player1, List.of(discarded));
        harness.setHand(player2, List.of(kept));

        harness.enterBattlefieldAndReturn(player2, new NezumiInformant());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(kept);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
