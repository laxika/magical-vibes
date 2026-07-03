package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardTransformIfCreatureDiscardedEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DrawDiscardTransformIfCreatureDiscardedEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Draws a card, sets pending transform, then begins discard")
            void drawsAndSetsTransform() {
                Card card = createCard("Civilized Scholar");
                DrawDiscardTransformIfCreatureDiscardedEffect effect = new DrawDiscardTransformIfCreatureDiscardedEffect();
                UUID sourcePermanentId = UUID.randomUUID();
                StackEntry entry = createTriggeredEntry(card, player1Id, List.of(effect), sourcePermanentId);
                gd.playerHands.get(player1Id).add(createCard("Mountain"));

                resolveEffect(gd, entry, effect);

                verify(drawService).resolveDrawCard(gd, player1Id);
                assertThat(gd.pendingTransformOnCreatureDiscard).isNotNull();
                assertThat(gd.pendingTransformOnCreatureDiscard.sourcePermanentId()).isEqualTo(sourcePermanentId);
                assertThat(gd.discardCausedByOpponent).isFalse();
                verify(playerInputService).beginDiscardChoice(eq(gd), eq(player1Id), anyInt());
            }

            @Test
            @DisplayName("Does not set transform when source permanent ID is null")
            void noTransformWhenNullSourcePermanent() {
                Card card = createCard("Civilized Scholar");
                DrawDiscardTransformIfCreatureDiscardedEffect effect = new DrawDiscardTransformIfCreatureDiscardedEffect();
                StackEntry entry = createEntry(card, player1Id, List.of(effect));
                gd.playerHands.get(player1Id).add(createCard("Mountain"));

                resolveEffect(gd, entry, effect);

                verify(drawService).resolveDrawCard(gd, player1Id);
                assertThat(gd.pendingTransformOnCreatureDiscard).isNull();
            }
}
