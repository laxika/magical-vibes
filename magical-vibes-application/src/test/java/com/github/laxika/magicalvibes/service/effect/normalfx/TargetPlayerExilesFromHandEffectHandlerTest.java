package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerExilesFromHandEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TargetPlayerExilesFromHandEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Begins exile from hand choice when target has cards")
            void beginsExileChoice() {
                Card card = createCard("Sin Collector");
                TargetPlayerExilesFromHandEffect effect = new TargetPlayerExilesFromHandEffect(1);
                StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);
                gd.playerHands.get(player2Id).add(createCard("Lightning Bolt"));

                Permanent sourcePermanent = new Permanent(card);
                gd.playerBattlefields.get(player1Id).add(sourcePermanent);

                resolveEffect(gd, entry, effect);

                verify(playerInputService).beginExileFromHandChoice(eq(gd), eq(player2Id), any(), anyInt());
            }

            @Test
            @DisplayName("Logs and does nothing when target hand is empty")
            void emptyTargetHand() {
                Card card = createCard("Sin Collector");
                TargetPlayerExilesFromHandEffect effect = new TargetPlayerExilesFromHandEffect(1);
                StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

                resolveEffect(gd, entry, effect);

                verify(playerInputService, never()).beginExileFromHandChoice(any(), any(), any(), anyInt());
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("no cards to exile")));
            }
}
