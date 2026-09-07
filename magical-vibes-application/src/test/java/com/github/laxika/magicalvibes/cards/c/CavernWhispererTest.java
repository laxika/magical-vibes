package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CavernWhisperer.class, GrizzlyBears.class})
class CavernWhispererTest extends BaseCardTest {

    @Test
    @DisplayName("Mutating makes each opponent discard a card")
    void mutatingMakesEachOpponentDiscardACard() {
        Permanent whisperer = addCreatureReady(player1, new CavernWhisperer());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        triggerMutation(whisperer);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Mutating does nothing when an opponent has no cards")
    void mutatingDoesNothingForEmptyOpponentHand() {
        Permanent whisperer = addCreatureReady(player1, new CavernWhisperer());
        harness.setHand(player2, new ArrayList<>());

        triggerMutation(whisperer);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void triggerMutation(Permanent whisperer) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, whisperer, List.of(whisperer.getCard()), player1.getId()));
        resolveAllTriggers();
    }
}
