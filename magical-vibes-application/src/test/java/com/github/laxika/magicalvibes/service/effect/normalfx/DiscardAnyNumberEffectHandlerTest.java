package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DiscardAnyNumberEffect;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class DiscardAnyNumberEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
    @DisplayName("Begins a number choice capped by the controller's hand size")
    void beginsNumberChoice() {
        Card card = createCard("Sacred Rites");
        DiscardAnyNumberEffect effect = new DiscardAnyNumberEffect();
        StackEntry entry = createEntry(card, player1Id, List.of(effect));
        gd.playerHands.get(player1Id).addAll(List.of(createCard("A"), createCard("B")));

        resolveEffect(gd, entry, effect);

        verify(interactionHandlerRegistry).begin(eq(gd),
                org.mockito.ArgumentMatchers.argThat(interaction ->
                        interaction instanceof PendingInteraction.XValueChoice choice
                                && choice.playerId().equals(player1Id)
                                && choice.maxValue() == 2));
    }

    @Test
    @DisplayName("Records the chosen discard count and starts the discard selection")
    void recordsChosenCount() {
        Card card = createCard("Sacred Rites");
        DiscardAnyNumberEffect effect = new DiscardAnyNumberEffect();
        StackEntry entry = createEntry(card, player1Id, List.of(effect));
        gd.playerHands.get(player1Id).addAll(List.of(createCard("A"), createCard("B")));
        gd.chosenXValue = 2;

        resolveEffect(gd, entry, effect);

        assertThat(entry.getEventValue()).isEqualTo(2);
        assertThat(gd.chosenXValue).isNull();
        verify(playerInputService).beginDiscardChoice(eq(gd), eq(player1Id), eq(2),
                eq(DiscardFollowUp.NONE));
    }

    @Test
    @DisplayName("Random mode discards the chosen number without opening a card selection")
    void randomModeDiscardsWithoutCardSelection() {
        Card card = createCard("Rites of Initiation");
        DiscardAnyNumberEffect effect = new DiscardAnyNumberEffect(true);
        StackEntry entry = createEntry(card, player1Id, List.of(effect));
        gd.playerHands.get(player1Id).addAll(List.of(createCard("A"), createCard("B")));
        gd.chosenXValue = 2;

        resolveEffect(gd, entry, effect);

        assertThat(entry.getEventValue()).isEqualTo(2);
        assertThat(gd.playerHands.get(player1Id)).isEmpty();
        verify(graveyardService, times(2)).discardCard(eq(gd), eq(player1Id), any(Card.class));
        verify(playerInputService, never()).beginDiscardChoice(any(), any(), anyInt(), any());
    }

    @Test
    @DisplayName("A zero choice records zero without opening a discard selection")
    void recordsZeroChoice() {
        Card card = createCard("Sacred Rites");
        DiscardAnyNumberEffect effect = new DiscardAnyNumberEffect();
        StackEntry entry = createEntry(card, player1Id, List.of(effect));
        gd.chosenXValue = 0;

        resolveEffect(gd, entry, effect);

        assertThat(entry.getEventValue()).isZero();
        verify(playerInputService, never()).beginDiscardChoice(any(), any(), anyInt(), any());
    }
}
