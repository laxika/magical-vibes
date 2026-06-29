package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DiscardUpToThenDrawThatManyEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DiscardUpToThenDrawThatManyEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Begins X value choice when hand is not empty")
            void beginsXValueChoice() {
                Card card = createCard("Faithful Mending");
                DiscardUpToThenDrawThatManyEffect effect = new DiscardUpToThenDrawThatManyEffect(3);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));
                gd.playerHands.get(player1Id).addAll(List.of(createCard("A"), createCard("B")));

                resolveEffect(gd, entry, effect);

                verify(playerInputService).beginXValueChoice(eq(gd), eq(player1Id), eq(2), any(), any());
            }

            @Test
            @DisplayName("Does nothing when hand is empty")
            void emptyHand() {
                Card card = createCard("Faithful Mending");
                DiscardUpToThenDrawThatManyEffect effect = new DiscardUpToThenDrawThatManyEffect(3);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));

                resolveEffect(gd, entry, effect);

                verify(playerInputService, never()).beginXValueChoice(any(), any(), any(int.class), any(), any());
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("no cards to discard")));
            }

            @Test
            @DisplayName("On re-entry with chosenXValue, sets up discard")
            void reEntryWithChosenXValue() {
                Card card = createCard("Faithful Mending");
                DiscardUpToThenDrawThatManyEffect effect = new DiscardUpToThenDrawThatManyEffect(3);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));
                gd.playerHands.get(player1Id).addAll(List.of(createCard("A"), createCard("B")));
                gd.chosenXValue = 2;

                resolveEffect(gd, entry, effect);

                assertThat(gd.pendingRummageDrawCount).isEqualTo(2);
                assertThat(gd.chosenXValue).isNull();
                verify(playerInputService).beginDiscardChoice(eq(gd), eq(player1Id));
            }

            @Test
            @DisplayName("On re-entry with chosenXValue of 0, does nothing")
            void reEntryWithZeroChosen() {
                Card card = createCard("Faithful Mending");
                DiscardUpToThenDrawThatManyEffect effect = new DiscardUpToThenDrawThatManyEffect(3);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));
                gd.chosenXValue = 0;

                resolveEffect(gd, entry, effect);

                assertThat(gd.chosenXValue).isNull();
                verify(playerInputService, never()).beginDiscardChoice(any(), any());
            }
}
