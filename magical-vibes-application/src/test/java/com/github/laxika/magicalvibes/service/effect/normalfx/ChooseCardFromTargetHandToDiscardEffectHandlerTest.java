package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromTargetHandToDiscardEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ChooseCardFromTargetHandToDiscardEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Reveals hand and begins choice when target has valid cards")
            void revealsHandAndBeginsChoice() {
                Card card = createCard("Thoughtseize");
                ChooseCardFromTargetHandToDiscardEffect effect = new ChooseCardFromTargetHandToDiscardEffect(1, List.of(CardType.LAND));
                StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);
                Card targetCard = createCard("Lightning Bolt");
                targetCard.setType(CardType.INSTANT);
                gd.playerHands.get(player2Id).add(targetCard);

                resolveEffect(gd, entry, effect);

                assertThat(gd.discardCausedByOpponent).isTrue();
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("reveals their hand")));
                verify(interactionHandlerRegistry).begin(eq(gd), argThat(i ->
                        i instanceof PendingInteraction.RevealedHandChoice rhc
                                && rhc.choosingPlayerId().equals(player1Id)
                                && rhc.targetPlayerId().equals(player2Id)));
            }

            @Test
            @DisplayName("Logs empty hand when target has no cards")
            void emptyHand() {
                Card card = createCard("Thoughtseize");
                ChooseCardFromTargetHandToDiscardEffect effect = new ChooseCardFromTargetHandToDiscardEffect(1, List.of(CardType.LAND));
                StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

                resolveEffect(gd, entry, effect);

                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("hand") && msg.contains("empty")));
            }
}
