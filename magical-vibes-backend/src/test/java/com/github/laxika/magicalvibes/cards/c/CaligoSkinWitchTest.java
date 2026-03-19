package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CaligoSkinWitchTest extends BaseCardTest {

    // ===== Cast without kicker =====

    @Test
    @DisplayName("Cast without kicker — enters as 1/3, no discard trigger")
    void castWithoutKickerNoDiscard() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));
        harness.setHand(player1, List.of(new CaligoSkinWitch()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 1); // generic

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        // Creature entered the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Caligo Skin-Witch"));
        // No ETB trigger on the stack
        assertThat(gd.stack).isEmpty();
        // Opponent still has all cards in hand
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    // ===== Cast with kicker =====

    @Test
    @DisplayName("Cast with kicker — ETB trigger goes on the stack")
    void castWithKickerPutsEtbOnStack() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears())));
        harness.setHand(player1, List.of(new CaligoSkinWitch()));
        harness.addMana(player1, ManaColor.BLACK, 2); // {B} for base + {B} for kicker
        harness.addMana(player1, ManaColor.WHITE, 4); // 1 generic for base + 3 generic for kicker

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        // Creature entered the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Caligo Skin-Witch"));
        // ETB trigger is on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("Cast with kicker — each opponent discards two cards")
    void castWithKickerOpponentDiscardsTwo() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears())));
        harness.setHand(player1, List.of(new CaligoSkinWitch()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Opponent must discard
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);
        assertThat(gd.interaction.cardChoice().playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.revealedHandChoice().discardRemainingCount()).isEqualTo(2);

        harness.handleCardChosen(player2, 0); // discard first card
        harness.handleCardChosen(player2, 0); // discard second card

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cast with kicker — opponent with empty hand, no discard needed")
    void castWithKickerEmptyOpponentHand() {
        harness.setHand(player2, new ArrayList<>());
        harness.setHand(player1, List.of(new CaligoSkinWitch()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("no cards to discard"));
    }
}
