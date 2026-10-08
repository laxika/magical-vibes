package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoldenBear;
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

@CardUsed({AbyssalNightstalker.class, Forest.class, GoldenBear.class})
class AbyssalNightstalkerTest extends BaseCardTest {

    private Permanent addAttacker() {
        return addCreatureReady(player1, new AbyssalNightstalker());
    }

    @Test
    @DisplayName("Unblocked attacker makes the defending player discard a card")
    void unblockedForcesDiscard() {
        harness.setHand(player2, new ArrayList<>(List.of(new GoldenBear(), new Forest())));
        addAttacker();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        // The defending player chooses which card to discard.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Golden Bear");
    }

    @Test
    @DisplayName("Blocked attacker does not make the defending player discard")
    void blockedNoDiscard() {
        harness.setHand(player2, new ArrayList<>(List.of(new GoldenBear(), new Forest())));

        Permanent blocker = addCreatureReady(player2, new GoldenBear());

        addAttacker();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker), 0)));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Unblocked attacker with an empty-handed defender resolves harmlessly")
    void unblockedEmptyHandNoDiscard() {
        harness.setHand(player2, new ArrayList<>());
        addAttacker();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("no cards to discard")).isTrue();
    }

    @Test
    @DisplayName("Discard happens after blockers are declared and before combat damage")
    void discardBeforeCombatDamage() {
        harness.setHand(player2, List.of(new GoldenBear(), new Forest()));
        addCreatureReady(player2, new GoldenBear());
        addAttacker();
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.DiscardChoice.class);

        gs.declareBlockers(gd, player2, List.of());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.handleCardChosen(player2, 1);

        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Player two's unblocked attacker makes player one discard")
    void playerTwoAttacks() {
        harness.setHand(player1, List.of(new GoldenBear(), new Forest()));
        harness.setHand(player2, List.of(new Forest()));
        addCreatureReady(player2, new AbyssalNightstalker());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Golden Bear");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }
}
