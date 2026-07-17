package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DrawAndRandomDiscardWithSharedTypeCountersEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DrawAndRandomDiscardWithSharedTypeCountersEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Draws cards and discards at random")
            void drawsAndDiscardsAtRandom() {
                Card card = createCard("Wild Mongrel");
                DrawAndRandomDiscardWithSharedTypeCountersEffect effect =
                        new DrawAndRandomDiscardWithSharedTypeCountersEffect(2, 2, 1);
                UUID sourcePermanentId = UUID.randomUUID();
                StackEntry entry = createTriggeredEntry(card, player1Id, List.of(effect), sourcePermanentId);
                gd.playerHands.get(player1Id).addAll(List.of(createCard("A"), createCard("B"), createCard("C")));

                resolveEffect(gd, entry, effect);

                verify(drawService, times(2)).resolveDrawCard(gd, player1Id);
                verify(graveyardService, times(2)).discardCard(eq(gd), eq(player1Id), any());
            }

            @Test
            @DisplayName("Sets discardCausedByOpponent to false")
            void setsDiscardNotByOpponent() {
                Card card = createCard("Wild Mongrel");
                DrawAndRandomDiscardWithSharedTypeCountersEffect effect =
                        new DrawAndRandomDiscardWithSharedTypeCountersEffect(1, 1, 1);
                UUID sourcePermanentId = UUID.randomUUID();
                StackEntry entry = createTriggeredEntry(card, player1Id, List.of(effect), sourcePermanentId);
                gd.playerHands.get(player1Id).add(createCard("A"));

                resolveEffect(gd, entry, effect);

                assertThat(gd.discardCausedByOpponent).isFalse();
            }
}
