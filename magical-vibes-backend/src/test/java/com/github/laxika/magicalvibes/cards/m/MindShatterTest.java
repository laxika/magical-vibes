package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerRandomDiscardXEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MindShatterTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Mind Shatter has correct card properties")
    void hasCorrectProperties() {
        MindShatter card = new MindShatter();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(TargetPlayerRandomDiscardXEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Mind Shatter targeting a player puts it on the stack with correct X value")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new MindShatter()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 3, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Mind Shatter");
        assertThat(entry.getXValue()).isEqualTo(3);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    // ===== Discard =====

    @Test
    @DisplayName("Resolving with X=3 discards 3 cards at random from target player's hand")
    void discardsXCardsAtRandom() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new SerraAngel(), new LightningBolt(), new GiantGrowth()));
        harness.setHand(player1, List.of(new MindShatter()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        // Target should have exactly 1 card remaining (started with 4, discarded 3)
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        // 3 cards should be in graveyard (discarded cards)
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("X=0 discards no cards")
    void xZeroDiscardsNothing() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new SerraAngel()));
        harness.setHand(player1, List.of(new MindShatter()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("X greater than hand size discards entire hand")
    void xGreaterThanHandSizeDiscardsAll() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new SerraAngel()));
        harness.setHand(player1, List.of(new MindShatter()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castSorcery(player1, 0, 5, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Targeting player with empty hand does nothing")
    void emptyHandDoesNothing() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new MindShatter()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetSelf() {
        harness.setHand(player1, new ArrayList<>(List.of(new MindShatter(), new GrizzlyBears(), new SerraAngel(), new LightningBolt())));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 2, player1.getId());
        harness.passBothPriorities();

        // Started with 4 cards, cast 1 (Mind Shatter), leaving 3, then discarded 2 at random
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    // ===== Graveyard and stack cleanup =====

    @Test
    @DisplayName("Mind Shatter goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new MindShatter()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Mind Shatter"));
    }

    @Test
    @DisplayName("Stack is empty after resolution")
    void stackIsEmptyAfterResolution() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new MindShatter()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }
}
