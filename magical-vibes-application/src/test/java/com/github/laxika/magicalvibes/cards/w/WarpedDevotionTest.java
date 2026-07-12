package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WarpedDevotionTest extends BaseCardTest {

    @Test
    @DisplayName("Bouncing an opponent's permanent makes its owner discard a card")
    void ownerDiscardsWhenPermanentBounced() {
        harness.addToBattlefield(player1, new WarpedDevotion());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, new ArrayList<>(List.of(new Forest())));

        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities(); // Boomerang resolves, bounces the bear, queues the trigger
        harness.passBothPriorities(); // Warped Devotion's trigger resolves

        // The bounced creature's owner (player2) chooses which card to discard.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The permanent's owner discards even when an opponent controls Warped Devotion")
    void controllerBouncingOwnPermanentDiscards() {
        // Warped Devotion is controlled by player1, but player1 bounces their own creature —
        // "that player" is the owner (player1), not the Warped Devotion controller.
        harness.addToBattlefield(player1, new WarpedDevotion());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, new ArrayList<>(List.of(new Boomerang(), new Forest())));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player1.getId());

        harness.handleCardChosen(player1, 0);
        // player1 discards the Forest; the bounced Grizzly Bears remains in hand.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Without Warped Devotion on the battlefield, bouncing causes no discard")
    void noDiscardWithoutWarpedDevotion() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, new ArrayList<>(List.of(new Forest())));

        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        // Only the bounced bear was added; nothing was discarded.
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
