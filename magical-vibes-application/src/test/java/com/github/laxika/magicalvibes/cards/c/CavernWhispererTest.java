package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AlmightyBrushwagg;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CavernWhisperer.class, AlmightyBrushwagg.class})
class CavernWhispererTest extends BaseCardTest {

    @Test
    @DisplayName("Mutating makes each opponent discard a card")
    void mutatingMakesEachOpponentDiscardACard() {
        Permanent whisperer = addCreatureReady(player1, new CavernWhisperer());
        harness.setHand(player2, new ArrayList<>(List.of(new AlmightyBrushwagg())));

        triggerMutation(whisperer);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Almighty Brushwagg");
    }

    @Test
    @DisplayName("Mutating does nothing when an opponent has no cards")
    void mutatingDoesNothingForEmptyOpponentHand() {
        Permanent whisperer = addCreatureReady(player1, new CavernWhisperer());
        harness.setHand(player2, new ArrayList<>());

        triggerMutation(whisperer);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The opponent chooses exactly one card and the controller keeps their hand")
    void opponentChoosesOneCard() {
        Permanent whisperer = addCreatureReady(player1, new CavernWhisperer());
        AlmightyBrushwagg kept = new AlmightyBrushwagg();
        CavernWhisperer discarded = new CavernWhisperer();
        AlmightyBrushwagg controllerCard = new AlmightyBrushwagg();
        harness.setHand(player1, List.of(controllerCard));
        harness.setHand(player2, List.of(kept, discarded));

        triggerMutation(whisperer);
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(kept);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(controllerCard);
        harness.assertInGraveyard(player2, "Cavern Whisperer");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Every mutation causes another discard")
    void repeatedMutationsCauseSeparateDiscards() {
        Permanent whisperer = addCreatureReady(player1, new CavernWhisperer());
        harness.setHand(player2, List.of(new AlmightyBrushwagg(), new CavernWhisperer()));

        triggerMutation(whisperer);
        harness.handleCardChosen(player2, 0);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);

        triggerMutation(whisperer);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Almighty Brushwagg");
        harness.assertInGraveyard(player2, "Cavern Whisperer");
    }

    @Test
    @DisplayName("Another creature mutating does not trigger Cavern Whisperer")
    void anotherCreatureMutatingDoesNotTrigger() {
        addCreatureReady(player1, new CavernWhisperer());
        Permanent other = addCreatureReady(player1, new AlmightyBrushwagg());
        AlmightyBrushwagg handCard = new AlmightyBrushwagg();
        harness.setHand(player2, List.of(handCard));

        triggerMutation(other);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handCard);
    }

    @Test
    @DisplayName("Casting normally does not make the opponent discard")
    void normalCastingDoesNotTriggerDiscard() {
        AlmightyBrushwagg handCard = new AlmightyBrushwagg();
        harness.setHand(player2, List.of(handCard));

        harness.castFromHand(player1, new CavernWhisperer(), "{4}{B}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Cavern Whisperer");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void triggerMutation(Permanent whisperer) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, whisperer, List.of(whisperer.getCard()), player1.getId()));
        resolveAllTriggers();
    }
}
