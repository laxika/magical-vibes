package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(FirestormPhoenix.class)
class FirestormPhoenixTest extends BaseCardTest {

    @Test
    @DisplayName("When Firestorm Phoenix would die, it returns to its owner's hand instead")
    void returnsToHandInsteadOfDying() {
        Permanent phoenix = harness.addToBattlefieldAndReturn(player1, new FirestormPhoenix());
        Card phoenixCard = phoenix.getCard();
        Card otherPhoenix = new FirestormPhoenix();

        harness.setHand(player1, List.of(otherPhoenix));
        killPhoenix(phoenix);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(phoenix);
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(phoenixCard.getId()));
        assertThat(gd.playerHands.get(player1.getId())).contains(phoenixCard);

        harness.clearMessages();
        harness.publishState();

        assertThat(harness.getConn2().getSentMessages())
                .anyMatch(message -> message.contains("\"opponentHand\":[")
                        && message.contains(phoenixCard.getId().toString()));
    }

    @Test
    @DisplayName("Only the returned Firestorm Phoenix is barred from being played")
    void onlyReturnedCardCannotBePlayed() {
        Permanent phoenix = harness.addToBattlefieldAndReturn(player1, new FirestormPhoenix());
        Card returnedPhoenix = phoenix.getCard();
        Card otherPhoenix = new FirestormPhoenix();

        harness.setHand(player1, List.of(otherPhoenix));
        killPhoenix(phoenix);

        harness.addMana(player1, ManaColor.RED, 6);
        assertThatThrownBy(() -> harness.castCreature(player1, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(otherPhoenix.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(returnedPhoenix.getId()));
    }

    @Test
    @DisplayName("The play restriction ends when its owner begins their next turn")
    void playRestrictionEndsAtOwnersNextTurn() {
        Permanent phoenix = harness.addToBattlefieldAndReturn(player1, new FirestormPhoenix());
        Card returnedPhoenix = phoenix.getCard();

        harness.setHand(player1, List.of());
        killPhoenix(phoenix);

        harness.setHand(player2, List.of());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(returnedPhoenix.getId()));
    }

    @Test
    @DisplayName("A stolen Firestorm Phoenix returns to its owner's hand")
    void returnsToOwnersHandWhenControlledByAnotherPlayer() {
        Card phoenixCard = new FirestormPhoenix();
        phoenixCard.setOwnerId(player2.getId());
        Permanent phoenix = harness.addToBattlefieldAndReturn(player1, phoenixCard);
        gd.stolenCreatures.put(phoenix.getId(), player2.getId());

        killPhoenix(phoenix);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(phoenix);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(phoenixCard);
        assertThat(gd.playerHands.get(player2.getId())).contains(phoenixCard);
    }

    private void killPhoenix(Permanent phoenix) {
        phoenix.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();
    }
}
