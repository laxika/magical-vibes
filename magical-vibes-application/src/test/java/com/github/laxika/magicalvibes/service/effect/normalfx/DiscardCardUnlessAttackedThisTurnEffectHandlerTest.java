package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DiscardCardUnlessAttackedThisTurnEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DiscardCardUnlessAttackedThisTurnEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Skips discard when player attacked this turn")
            void skipsDiscardWhenAttacked() {
                Card card = createCard("Keldon Marauders");
                DiscardCardUnlessAttackedThisTurnEffect effect = new DiscardCardUnlessAttackedThisTurnEffect();
                StackEntry entry = createEntry(card, player1Id, List.of(effect));
                gd.playersDeclaredAttackersThisTurn.add(player1Id);

                resolveEffect(gd, entry, new DiscardCardUnlessAttackedThisTurnEffect());

                verify(playerInputService, never()).beginDiscardChoice(any(), any(), anyInt());
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("attacked this turn")));
            }

            @Test
            @DisplayName("Forces discard when player did not attack")
            void forcesDiscardWhenDidNotAttack() {
                Card card = createCard("Keldon Marauders");
                DiscardCardUnlessAttackedThisTurnEffect effect = new DiscardCardUnlessAttackedThisTurnEffect();
                StackEntry entry = createEntry(card, player1Id, List.of(effect));
                gd.playerHands.get(player1Id).add(createCard("Mountain"));

                resolveEffect(gd, entry, new DiscardCardUnlessAttackedThisTurnEffect());

                assertThat(gd.discardCausedByOpponent).isFalse();
                verify(playerInputService).beginDiscardChoice(eq(gd), eq(player1Id), anyInt());
            }
}
