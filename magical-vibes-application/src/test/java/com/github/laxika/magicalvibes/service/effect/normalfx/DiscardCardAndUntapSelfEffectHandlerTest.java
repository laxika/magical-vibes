package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DiscardCardAndUntapSelfEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DiscardCardAndUntapSelfEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Sets pending untap permanent ID and begins discard")
            void setsUntapAndBeginsDiscard() {
                Card card = createCard("Merfolk Looter");
                DiscardCardAndUntapSelfEffect effect = new DiscardCardAndUntapSelfEffect();
                UUID sourcePermanentId = UUID.randomUUID();
                StackEntry entry = createTriggeredEntry(card, player1Id, List.of(effect), sourcePermanentId);
                gd.playerHands.get(player1Id).add(createCard("Mountain"));

                resolveEffect(gd, entry, effect);

                assertThat(gd.pendingUntapAfterDiscardPermanentId).isEqualTo(sourcePermanentId);
                assertThat(gd.discardCausedByOpponent).isFalse();
                verify(playerInputService).beginDiscardChoice(eq(gd), eq(player1Id), anyInt());
            }

            @Test
            @DisplayName("Does nothing when hand is empty")
            void doesNothingWhenHandEmpty() {
                Card card = createCard("Merfolk Looter");
                DiscardCardAndUntapSelfEffect effect = new DiscardCardAndUntapSelfEffect();
                StackEntry entry = createEntry(card, player1Id, List.of(effect));

                resolveEffect(gd, entry, effect);

                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("no cards to discard")));
            }
}
