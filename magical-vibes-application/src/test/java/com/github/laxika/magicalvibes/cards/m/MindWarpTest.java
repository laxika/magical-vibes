package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MindWarpTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack with the chosen X value and target")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new MindWarp()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, 2, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Mind Warp");
        assertThat(entry.getXValue()).isEqualTo(2);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    // ===== Resolving — caster chooses X cards to discard =====

    @Test
    @DisplayName("Resolving with X=2 prompts the caster to choose two cards from the hand")
    void promptsForTwoChoices() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new SerraAngel(), new LightningBolt())));
        harness.setHand(player1, List.of(new MindWarp()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.choosingPlayerId()).isEqualTo(player1.getId());
        assertThat(choice.remainingCount()).isEqualTo(2);
        assertThat(choice.discardMode()).isTrue();
        // No type restriction — every card is a valid choice.
        assertThat(choice.validIndices()).containsExactly(0, 1, 2);
    }

    @Test
    @DisplayName("Choosing X cards discards exactly those cards to the target's graveyard")
    void choosingCardsDiscardsThem() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new SerraAngel(), new LightningBolt())));
        harness.setHand(player1, List.of(new MindWarp()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        // Choose Grizzly Bears then Serra Angel.
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).getFirst().getName()).isEqualTo("Lightning Bolt");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Grizzly Bears", "Serra Angel");
    }

    @Test
    @DisplayName("X greater than hand size discards the entire hand")
    void xGreaterThanHandDiscardsAll() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new SerraAngel())));
        harness.setHand(player1, List.of(new MindWarp()));
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.castSorcery(player1, 0, 4, player2.getId());
        harness.passBothPriorities();

        // Capped at the two cards actually in hand.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).remainingCount())
                .isEqualTo(2);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("X=0 discards nothing and prompts no choice")
    void xZeroDiscardsNothing() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new SerraAngel())));
        harness.setHand(player1, List.of(new MindWarp()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Targeting a player with an empty hand does nothing")
    void emptyHandDoesNothing() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new MindWarp()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    // ===== Targeting =====

    @Test
    @DisplayName("Can target yourself")
    void canTargetSelf() {
        harness.setHand(player1, new ArrayList<>(List.of(new MindWarp(), new GrizzlyBears(), new GiantGrowth())));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 1, player1.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Validation =====

    @Test
    @DisplayName("Invalid card index is rejected")
    void invalidCardIndexRejected() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new SerraAngel())));
        harness.setHand(player1, List.of(new MindWarp()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 5))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    // ===== After resolution =====

    @Test
    @DisplayName("Mind Warp goes to the caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setHand(player1, List.of(new MindWarp()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 1, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Mind Warp"));
    }
}
