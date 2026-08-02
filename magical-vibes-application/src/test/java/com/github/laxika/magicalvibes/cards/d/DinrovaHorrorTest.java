package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DinrovaHorrorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB bounces the target and its owner discards a card")
    void bouncesTargetAndOwnerDiscards() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player2, new ArrayList<>(List.of(new Peek())));

        cast(targetId);

        harness.assertOnBattlefield(player1, "Dinrova Horror");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());

        // Hand is Peek + the bounced Grizzly Bears; discard the returned creature.
        harness.handleCardChosen(player2, indexOf(gd.playerHands.get(player2.getId()), "Grizzly Bears"));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId()))
                .hasSize(1)
                .anyMatch(c -> c.getName().equals("Peek"));
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A land is a legal target; its owner then discards")
    void canTargetLand() {
        harness.addToBattlefield(player2, new Island());
        UUID targetId = harness.getPermanentId(player2, "Island");
        harness.setHand(player2, new ArrayList<>(List.of(new Peek())));

        cast(targetId);

        harness.assertNotOnBattlefield(player2, "Island");
        harness.assertInHand(player2, "Island");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
    }

    @Test
    @DisplayName("Targeting your own permanent makes you discard")
    void targetingOwnPermanentDiscardsSelf() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        cast(targetId);

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("With an otherwise empty hand, the bounced card is the forced discard")
    void bouncedCardIsForcedDiscard() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player2, new ArrayList<>());

        cast(targetId);

        // The bounced card is the only card in hand, so it is discarded.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private void cast(UUID targetId) {
        harness.setHand(player1, new ArrayList<>(List.of(new DinrovaHorror())));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger
        harness.passBothPriorities(); // resolve ETB trigger
    }

    private static int indexOf(List<? extends Card> hand, String name) {
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getName().equals(name)) {
                return i;
            }
        }
        throw new AssertionError("Card not in hand: " + name);
    }
}
