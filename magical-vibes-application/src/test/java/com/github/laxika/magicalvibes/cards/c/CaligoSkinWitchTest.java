package com.github.laxika.magicalvibes.cards.c;


import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.p.PrimordialWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaligoSkinWitch.class, PrimordialWurm.class, CastDown.class})
class CaligoSkinWitchTest extends BaseCardTest {

    // ===== Cast without kicker =====

    @Test
    @DisplayName("Cast without kicker — enters as 1/3, no discard trigger")
    void castWithoutKickerNoDiscard() {
        harness.setHand(player2, new ArrayList<>(List.of(new PrimordialWurm(), new PrimordialWurm())));
        harness.setHand(player1, List.of(new CaligoSkinWitch()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 1); // generic

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        // Creature entered the battlefield
        harness.assertOnBattlefield(player1, "Caligo Skin-Witch");
        // No ETB trigger on the stack
        assertThat(gd.stack).isEmpty();
        // Opponent still has all cards in hand
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    // ===== Cast with kicker =====

    @Test
    @DisplayName("Cast with kicker — ETB trigger goes on the stack")
    void castWithKickerPutsEtbOnStack() {
        harness.setHand(player2, new ArrayList<>(List.of(new PrimordialWurm(), new PrimordialWurm(), new PrimordialWurm())));
        harness.setHand(player1, List.of(new CaligoSkinWitch()));
        harness.addMana(player1, ManaColor.BLACK, 2); // {B} for base + {B} for kicker
        harness.addMana(player1, ManaColor.WHITE, 4); // 1 generic for base + 3 generic for kicker

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        // Creature entered the battlefield
        harness.assertOnBattlefield(player1, "Caligo Skin-Witch");
        // ETB trigger is on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("Cast with kicker — each opponent discards two cards")
    void castWithKickerOpponentDiscardsTwo() {
        harness.setHand(player2, new ArrayList<>(List.of(new PrimordialWurm(), new PrimordialWurm(), new PrimordialWurm())));
        harness.setHand(player1, List.of(new CaligoSkinWitch()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        // Opponent must discard
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player2, 0); // discard first card
        harness.handleCardChosen(player2, 0); // discard second card

        assertThat(gd.interaction.activeInteraction()).isNull();
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
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("no cards to discard")).isTrue();
    }

    @Test
    @DisplayName("Kicked creature makes an opponent with one card discard that card")
    void opponentDiscardsOnlyAvailableCard() {
        PrimordialWurm discarded = new PrimordialWurm();
        PrimordialWurm retained = new PrimordialWurm();
        harness.setHand(player2, List.of(discarded));
        harness.setHand(player1, List.of(new CaligoSkinWitch(), retained));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Kicked discard trigger resolves after its source is destroyed")
    void discardTriggerSurvivesSourceRemoval() {
        harness.setHand(player2, List.of(new CastDown(), new PrimordialWurm(), new PrimordialWurm()));
        harness.setHand(player1, List.of(new CaligoSkinWitch()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player2, 0, gd.playerBattlefields.get(player1.getId()).getFirst().getId());
        harness.assertInGraveyard(player1, "Caligo Skin-Witch");
        harness.assertNotOnBattlefield(player1, "Caligo Skin-Witch");

        resolveAllTriggers();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
