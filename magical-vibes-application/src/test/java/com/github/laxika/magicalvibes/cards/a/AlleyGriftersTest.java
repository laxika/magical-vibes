package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlleyGrifters.class, GrizzlyBears.class, Forest.class})
class AlleyGriftersTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming blocked makes the defending player discard a card")
    void blockedForcesDefendingPlayerDiscard() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));
        addAttackingGrifters();
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("An unblocked Alley Grifters causes no discard")
    void unblockedNoDiscard() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));
        addAttackingGrifters();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The controller's hand is untouched when Alley Grifters is blocked")
    void controllerDoesNotDiscard() {
        harness.setHand(player1, new ArrayList<>(List.of(new Forest())));
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        addAttackingGrifters();
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Multiple blockers still cause only one discard")
    void multipleBlockersCauseOneDiscard() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));
        addAttackingGrifters();
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A blocked Alley Grifters does nothing when the defending player has no cards")
    void blockedWithEmptyDefendingHandDoesNothing() {
        harness.setHand(player2, new ArrayList<>());
        addAttackingGrifters();
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    private void addAttackingGrifters() {
        Permanent perm = addCreatureReady(player1, new AlleyGrifters());
        perm.setAttacking(true);
        perm.setAttackTarget(player2.getId());
    }
}
