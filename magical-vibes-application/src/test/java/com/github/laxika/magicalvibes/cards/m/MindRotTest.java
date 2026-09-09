package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameLogSegment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Forest.class, GrizzlyBears.class, MindRot.class})
class MindRotTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Mind Rot puts it on the stack")
    void castingPutsOnStack() {
        MindRot spell = new MindRot();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard()).isSameAs(spell);
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Resolving prompts target player to discard")
    void resolvingPromptsTargetPlayerToDiscard() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new MindRot(), new Forest()));
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Target player (player2) should be prompted to discard, not the caster.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Target discards two cards of their choice")
    void targetDiscardsTwoCards() {
        GrizzlyBears firstDiscard = new GrizzlyBears();
        MindRot secondDiscard = new MindRot();
        Forest remainingCard = new Forest();
        harness.setHand(player2, List.of(firstDiscard, secondDiscard, remainingCard));
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Target player chooses first discard.
        harness.handleCardChosen(player2, 0);

        // Still awaiting second discard.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player2.getId());

        // Target player chooses second discard.
        harness.handleCardChosen(player2, 0);

        // Discard complete.
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(remainingCard);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactlyInAnyOrder(firstDiscard, secondDiscard);
    }

    @Test
    @DisplayName("Target can choose any cards including lands")
    void targetCanChooseLands() {
        Forest discardedLand = new Forest();
        GrizzlyBears discardedCreature = new GrizzlyBears();
        Forest remainingLand = new Forest();
        harness.setHand(player2, List.of(discardedLand, discardedCreature, remainingLand));
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // All indices should be valid because Mind Rot doesn't restrict card types.
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactlyInAnyOrder(0, 1, 2);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(remainingLand);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactlyInAnyOrder(discardedLand, discardedCreature);
    }

    @Test
    @DisplayName("Mind Rot goes to caster's graveyard after resolving")
    void goesToCasterGraveyardAfterResolving() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new MindRot()));
        MindRot spell = new MindRot();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
    }

    @Test
    @DisplayName("Target with one card discards it then discard ends")
    void targetWithOneCardDiscardsIt() {
        GrizzlyBears onlyCard = new GrizzlyBears();
        harness.setHand(player2, List.of(onlyCard));
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player2, 0);

        // Hand is now empty, so the second discard is skipped.
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(onlyCard);
    }

    @Test
    @DisplayName("Target with empty hand results in no discard prompt")
    void targetWithEmptyHandNoPrompt() {
        MindRot spell = new MindRot();
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // No discard prompt because the hand is empty.
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gameLogContains("no cards to discard")).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
    }

    @Test
    @DisplayName("Can target yourself to discard your own cards")
    void canTargetSelf() {
        MindRot spell = new MindRot();
        GrizzlyBears firstDiscard = new GrizzlyBears();
        MindRot secondDiscard = new MindRot();
        Forest remainingCard = new Forest();
        harness.setHand(player1, List.of(spell, firstDiscard, secondDiscard, remainingCard));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        // Player1 is prompted to discard from their own hand.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player1.getId());

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(spell, firstDiscard, secondDiscard);
    }

    @Test
    @DisplayName("Caster cannot make the discard choice for the target")
    void casterCannotChooseForTarget() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new MindRot()));
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn to choose");
    }

    @Test
    @DisplayName("Discarded cards are logged")
    void discardIsLogged() {
        GrizzlyBears firstDiscard = new GrizzlyBears();
        MindRot secondDiscard = new MindRot();
        harness.setHand(player2, List.of(firstDiscard, secondDiscard));
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.gameLog).anyMatch(entry -> entry.segments().stream().anyMatch(segment ->
                segment instanceof GameLogSegment.CardSegment cardSegment
                        && cardSegment.card() == firstDiscard));
        assertThat(gd.gameLog).anyMatch(entry -> entry.segments().stream().anyMatch(segment ->
                segment instanceof GameLogSegment.CardSegment cardSegment
                        && cardSegment.card() == secondDiscard));
    }
}
