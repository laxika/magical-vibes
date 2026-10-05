package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Mindculling.class, Forest.class, GrizzlyBears.class, Peek.class})
class MindcullingTest extends BaseCardTest {

    @Test
    @DisplayName("Draws from caster's library before opponent chooses any two cards to discard")
    void drawsBeforeOpponentChoosesDiscards() {
        Mindculling spell = new Mindculling();
        Forest keptByCaster = new Forest();
        Forest firstDraw = new Forest();
        Mindculling secondDraw = new Mindculling();
        Forest firstDiscard = new Forest();
        Mindculling keptByOpponent = new Mindculling();
        Forest secondDiscard = new Forest();
        harness.setHand(player1, List.of(spell, keptByCaster));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player2, List.of(firstDiscard, keptByOpponent, secondDiscard));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(keptByCaster, firstDraw, secondDraw);
        assertThat(gd.playerHands.get(player2.getId()))
                .containsExactly(firstDiscard, keptByOpponent, secondDiscard);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.handleCardChosen(player2, 2);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(keptByOpponent);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactlyInAnyOrder(firstDiscard, secondDiscard);
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(keptByCaster, firstDraw, secondDraw);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new Mindculling()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Caster draws two cards and target opponent is prompted to discard two")
    void drawsAndOpponentDiscards() {
        harness.setHand(player1, List.of(new Mindculling()));
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek(), new Forest())));
        harness.addMana(player1, ManaColor.BLUE, 6);

        int player1HandBefore = gd.playerHands.get(player1.getId()).size();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Caster should have drawn 2 cards (had 0 after casting, now 2)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(player1HandBefore - 1 + 2);

        // Target player should be prompted to discard
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Opponent with empty hand still lets caster draw two")
    void opponentEmptyHandStillDraws() {
        harness.setHand(player1, List.of(new Mindculling()));
        harness.setHand(player2, new ArrayList<>(List.of()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Caster still draws 2
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        // No discard prompt
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Opponent with one card discards it then discard ends")
    void opponentWithOneCardDiscardsIt() {
        harness.setHand(player1, List.of(new Mindculling()));
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Caster drew 2
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        // Opponent prompted to discard
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        // Hand empty, second discard skipped
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Mindculling goes to caster's graveyard after resolving")
    void goesToCasterGraveyard() {
        harness.setHand(player1, List.of(new Mindculling()));
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Mindculling");
    }
}
