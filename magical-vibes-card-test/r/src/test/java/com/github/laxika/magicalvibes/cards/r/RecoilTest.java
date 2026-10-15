package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.d.Dodecapod;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Recoil.class, RagingKavu.class, Island.class, Dodecapod.class})
class RecoilTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a permanent and its owner discards a card")
    void returnsPermanentAndOwnerDiscards() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new RagingKavu()).getId();
        harness.setHand(player2, List.of(new Island()));

        castAt(targetId);

        harness.assertNotOnBattlefield(player2, "Raging Kavu");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, indexOf(gd.playerHands.get(player2.getId()), RagingKavu.class));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInHand(player2, "Island");
        harness.assertInGraveyard(player2, "Raging Kavu");
    }

    @Test
    @DisplayName("Can target a land")
    void canTargetLand() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Island()).getId();
        harness.setHand(player2, List.of(new RagingKavu()));

        castAt(targetId);

        harness.assertNotOnBattlefield(player2, "Island");
        harness.assertInHand(player2, "Island");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
    }

    @Test
    @DisplayName("Targeting your own permanent makes you discard")
    void targetingOwnPermanentMakesYouDiscard() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new RagingKavu()).getId();

        castAt(targetId);

        harness.assertInHand(player1, "Raging Kavu");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("An empty-handed owner discards the permanent just returned")
    void emptyHandDiscardsReturnedPermanent() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new RagingKavu()).getId();
        harness.setHand(player2, List.of());

        castAt(targetId);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Raging Kavu");
        harness.assertNotOnBattlefield(player2, "Raging Kavu");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A stolen permanent returns to its owner, who discards")
    void stolenPermanentMakesOwnerDiscard() {
        RagingKavu card = new RagingKavu();
        card.setOwnerId(player2.getId());
        UUID targetId = harness.addToBattlefieldAndReturn(player1, card).getId();
        gd.stolenCreatures.put(targetId, player2.getId());
        harness.setHand(player2, List.of());

        castAt(targetId);

        harness.assertInHand(player2, "Raging Kavu");
        harness.assertNotInHand(player1, "Raging Kavu");
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.assertInGraveyard(player2, "Raging Kavu");
        harness.assertNotOnBattlefield(player1, "Raging Kavu");
    }

    @Test
    @DisplayName("An illegal target prevents both the return and the discard")
    void missingTargetDoesNotCauseDiscard() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new RagingKavu()).getId();
        harness.setHand(player2, List.of(new Island()));
        harness.setHand(player1, List.of(new Recoil()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, targetId);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, findPermanent(player2, "Raging Kavu")));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        harness.assertInHand(player2, "Island");
        harness.assertInHand(player2, "Raging Kavu");
        harness.assertInGraveyard(player1, "Recoil");
    }

    @Test
    @DisplayName("Dodecapod discarded to an opponent's Recoil enters with two counters")
    void opponentCausedDiscardAppliesDodecapodReplacement() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Island()).getId();
        harness.setHand(player2, List.of(new Dodecapod()));

        castAt(targetId);
        harness.handleCardChosen(player2, 0);

        harness.assertOnBattlefield(player2, "Dodecapod");
        harness.assertNotInGraveyard(player2, "Dodecapod");
        harness.assertInHand(player2, "Island");
        assertThat(findPermanent(player2, "Dodecapod").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
    }

    private void castAt(UUID targetId) {
        harness.setHand(player1, List.of(new Recoil()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, targetId);
    }

    private static int indexOf(List<? extends Card> hand, Class<? extends Card> cardType) {
        for (int i = 0; i < hand.size(); i++) {
            if (cardType.isInstance(hand.get(i))) {
                return i;
            }
        }
        throw new AssertionError("Card not in hand: " + cardType.getSimpleName());
    }
}
