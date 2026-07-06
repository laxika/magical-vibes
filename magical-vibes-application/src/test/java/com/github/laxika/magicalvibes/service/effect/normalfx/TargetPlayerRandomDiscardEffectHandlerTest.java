package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerRandomDiscardEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TargetPlayerRandomDiscardEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Target player discards at random with opponent flag")
            void targetDiscardsAtRandom() {
                Card card = createCard("Hypnotic Specter");
                TargetPlayerRandomDiscardEffect effect = new TargetPlayerRandomDiscardEffect(new Fixed(1), true);
                StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);
                gd.playerHands.get(player2Id).add(createCard("Mountain"));

                resolveEffect(gd, entry, effect);

                assertThat(gd.discardCausedByOpponent).isTrue();
                assertThat(gd.playerHands.get(player2Id)).isEmpty();
                verify(graveyardService).addCardToGraveyard(eq(gd), eq(player2Id), any());
            }

            @Test
            @DisplayName("Controller discards when causedByOpponent is false")
            void controllerDiscardsWhenNotOpponent() {
                Card card = createCard("Wild Mongrel");
                TargetPlayerRandomDiscardEffect effect = new TargetPlayerRandomDiscardEffect(new Fixed(1), false);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));
                gd.playerHands.get(player1Id).add(createCard("Mountain"));

                resolveEffect(gd, entry, effect);

                assertThat(gd.discardCausedByOpponent).isFalse();
                assertThat(gd.playerHands.get(player1Id)).isEmpty();
            }

            @Test
            @DisplayName("No discard when hand is empty")
            void noDiscardWhenHandEmpty() {
                Card card = createCard("Hypnotic Specter");
                TargetPlayerRandomDiscardEffect effect = new TargetPlayerRandomDiscardEffect(new Fixed(1), true);
                StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

                resolveEffect(gd, entry, effect);

                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("no cards to discard")));
            }
}
