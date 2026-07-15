package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.FlipTwoCoinsEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class FlipTwoCoinsEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Always broadcasts two coin flip results")
            void broadcastsTwoFlipResults() {
                Card card = createCard("Tavern Swindler");
                DrawCardEffect headsEffect = new DrawCardEffect(2);
                DiscardEffect tailsEffect = new DiscardEffect(1, DiscardRecipient.CONTROLLER);
                FlipTwoCoinsEffect effect = new FlipTwoCoinsEffect(headsEffect, tailsEffect);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));

                resolveEffect(gd, entry, effect);

                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) ->
                        logEntry.plainText().contains("flips two coins") && logEntry.plainText().contains("Player1")));
            }
}
