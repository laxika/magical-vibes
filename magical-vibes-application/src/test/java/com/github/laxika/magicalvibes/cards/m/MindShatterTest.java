package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.i.IndomitableAncients;
import com.github.laxika.magicalvibes.cards.p.PricklyBoggart;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MindShatter.class, ElvishWarrior.class, IndomitableAncients.class,
        MudbuttonClanger.class, PricklyBoggart.class})
class MindShatterTest extends BaseCardTest {

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
        assertThat(entry.getXValue()).isEqualTo(3);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    // ===== Discard =====

    @Test
    @DisplayName("Resolving with X=3 discards 3 cards at random from target player's hand")
    void discardsXCardsAtRandom() {
        harness.setHand(player2, List.of(new ElvishWarrior(), new IndomitableAncients(),
                new MudbuttonClanger(), new PricklyBoggart()));
        harness.setHand(player1, List.of(new MindShatter()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        // Target should have exactly 1 card remaining (started with 4, discarded 3)
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        // 3 cards should be in graveyard (discarded cards)
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.gameLog).anyMatch(log -> log.plainText().contains("discards")
                && log.plainText().contains("at random"));
    }

    @Test
    @DisplayName("X=0 discards no cards")
    void xZeroDiscardsNothing() {
        harness.setHand(player2, List.of(new ElvishWarrior(), new IndomitableAncients()));
        harness.setHand(player1, List.of(new MindShatter()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("X greater than hand size discards entire hand")
    void xGreaterThanHandSizeDiscardsAll() {
        harness.setHand(player2, List.of(new ElvishWarrior(), new IndomitableAncients()));
        harness.setHand(player1, List.of(new MindShatter()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castAndResolveSorcery(player1, 0, 5, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Targeting player with empty hand does nothing")
    void emptyHandDoesNothing() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new MindShatter()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 2, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        harness.setHand(player1, List.of(new MindShatter()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetSelf() {
        harness.setHand(player1, List.of(new MindShatter(), new ElvishWarrior(),
                new IndomitableAncients(), new MudbuttonClanger()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 2, player1.getId());

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

        harness.castAndResolveSorcery(player1, 0, 2, player2.getId());

        harness.assertInGraveyard(player1, "Mind Shatter");
    }

    @Test
    @DisplayName("Stack is empty after resolution")
    void stackIsEmptyAfterResolution() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new MindShatter()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 2, player2.getId());

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Random discard uses the target's hand at resolution and preserves every card")
    void discardsFromHandAtResolution() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new MindShatter()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorcery(player1, 0, 1, player2.getId());

        List<Card> cards = List.of(new ElvishWarrior(), new IndomitableAncients(),
                new MudbuttonClanger(), new PricklyBoggart());
        harness.setHand(player2, cards);
        harness.passBothPriorities();

        var hand = gd.playerHands.get(player2.getId());
        var graveyard = gd.playerGraveyards.get(player2.getId());
        assertThat(hand).hasSize(3);
        assertThat(graveyard).hasSize(1);
        assertThat(java.util.stream.Stream.concat(hand.stream(), graveyard.stream()).toList())
                .containsExactlyInAnyOrderElementsOf(cards);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Mind Shatter");
    }
}
