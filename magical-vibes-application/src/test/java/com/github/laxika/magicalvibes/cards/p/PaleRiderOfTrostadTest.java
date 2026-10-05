package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GhoulcallersAccomplice;
import com.github.laxika.magicalvibes.cards.h.HulkingDevil;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PaleRiderOfTrostad.class, GhoulcallersAccomplice.class, HulkingDevil.class})
class PaleRiderOfTrostadTest extends BaseCardTest {

    @Test
    @DisplayName("When Pale Rider of Trostad enters, its controller discards a card")
    void enteringPromptsControllerDiscard() {
        GhoulcallersAccomplice discarded = new GhoulcallersAccomplice();
        harness.setHand(player1, List.of(new PaleRiderOfTrostad(), discarded));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player1.getId());

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Ghoulcaller's Accomplice");
    }

    @Test
    @DisplayName("Pale Rider of Trostad's ETB does nothing with an empty hand")
    void enteringDoesNothingWithEmptyHand() {
        harness.setHand(player1, List.of(new PaleRiderOfTrostad()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void controllerChoosesExactlyOneCardAndOpponentDoesNotDiscard() {
        GhoulcallersAccomplice kept = new GhoulcallersAccomplice();
        HulkingDevil discarded = new HulkingDevil();
        GhoulcallersAccomplice opponentCard = new GhoulcallersAccomplice();
        harness.setHand(player1, List.of(new PaleRiderOfTrostad(), kept, discarded));
        harness.setHand(player2, List.of(opponentCard));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Pale Rider of Trostad");
    }

    @Test
    void skulkPreventsGreaterPowerCreatureFromBlocking() {
        Permanent attacker = addCreatureReady(player1, new PaleRiderOfTrostad());
        Permanent blocker = addCreatureReady(player2, new HulkingDevil());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("skulk");
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    void skulkAllowsEqualPowerCreatureToBlock() {
        Permanent attacker = addCreatureReady(player1, new PaleRiderOfTrostad());
        Permanent blocker = addCreatureReady(player2, new PaleRiderOfTrostad());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void skulkAllowsLowerPowerCreatureToBlock() {
        Permanent attacker = addCreatureReady(player1, new PaleRiderOfTrostad());
        Permanent blocker = addCreatureReady(player2, new GhoulcallersAccomplice());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
