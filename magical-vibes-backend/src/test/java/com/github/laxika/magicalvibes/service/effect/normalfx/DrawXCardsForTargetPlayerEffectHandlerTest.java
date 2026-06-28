package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DrawXCardsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawXCardsForTargetPlayerEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DrawXCardsForTargetPlayerEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Target player draws X cards")
            void targetPlayerDrawsXCards() {
                Card card = createCard("Blue Sun's Zenith");
                DrawXCardsEffect effect = new DrawXCardsEffect();
                StackEntry entry = createEntryWithXValueAndTarget(card, player1Id, List.of(effect), 4, player2Id);

                resolveEffect(gd, entry, new DrawXCardsForTargetPlayerEffect());

                verify(drawService, times(4)).resolveDrawCard(gd, player2Id);
            }

            @Test
            @DisplayName("Logs draw message")
            void logsDrawMessage() {
                Card card = createCard("Blue Sun's Zenith");
                DrawXCardsEffect effect = new DrawXCardsEffect();
                StackEntry entry = createEntryWithXValueAndTarget(card, player1Id, List.of(effect), 3, player2Id);

                resolveEffect(gd, entry, new DrawXCardsForTargetPlayerEffect());

                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("Player2") && msg.contains("draws 3 cards")));
            }
}
