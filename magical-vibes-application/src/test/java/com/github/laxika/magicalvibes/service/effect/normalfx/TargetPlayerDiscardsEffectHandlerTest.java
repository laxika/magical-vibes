package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TargetPlayerDiscardsEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Target player discards with opponent flag set")
            void targetPlayerDiscardsWithOpponentFlag() {
                Card card = createCard("Mind Rot");
                TargetPlayerDiscardsEffect effect = new TargetPlayerDiscardsEffect(2);
                StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);
                gd.playerHands.get(player2Id).addAll(List.of(createCard("A"), createCard("B")));

                resolveEffect(gd, entry, effect);

                assertThat(gd.discardCausedByOpponent).isTrue();
                verify(playerInputService).beginDiscardChoice(eq(gd), eq(player2Id), anyInt(),
                        any(DiscardFollowUp.class));
            }

            @Test
            @DisplayName("Logs when target has empty hand")
            void logsWhenTargetHandEmpty() {
                Card card = createCard("Mind Rot");
                TargetPlayerDiscardsEffect effect = new TargetPlayerDiscardsEffect(2);
                StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

                resolveEffect(gd, entry, effect);

                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("no cards to discard")));
            }
}
