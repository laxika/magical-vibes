package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsByChargeCountersEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TargetPlayerDiscardsByChargeCountersEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Discards cards equal to charge counter count")
            void discardsBasedOnChargeCounters() {
                Card card = createCard("Shrine of Limitless Power");
                TargetPlayerDiscardsByChargeCountersEffect effect = new TargetPlayerDiscardsByChargeCountersEffect();
                StackEntry entry = createEntryWithXValueAndTarget(card, player1Id, List.of(effect), 3, player2Id);
                gd.playerHands.get(player2Id).addAll(List.of(createCard("A"), createCard("B"), createCard("C")));

                resolveEffect(gd, entry, new TargetPlayerDiscardsByChargeCountersEffect());

                assertThat(gd.discardCausedByOpponent).isTrue();
                verify(playerInputService).beginDiscardChoice(eq(gd), eq(player2Id), anyInt(),
                        any(DiscardFollowUp.class));
            }

            @Test
            @DisplayName("No discard when charge counter value is 0")
            void noDiscardWhenZeroChargeCounters() {
                Card card = createCard("Shrine of Limitless Power");
                TargetPlayerDiscardsByChargeCountersEffect effect = new TargetPlayerDiscardsByChargeCountersEffect();
                StackEntry entry = createEntryWithXValueAndTarget(card, player1Id, List.of(effect), 0, player2Id);

                resolveEffect(gd, entry, new TargetPlayerDiscardsByChargeCountersEffect());

                verify(playerInputService, never()).beginDiscardChoice(any(), any(), anyInt());
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("discards 0 cards")));
            }
}
