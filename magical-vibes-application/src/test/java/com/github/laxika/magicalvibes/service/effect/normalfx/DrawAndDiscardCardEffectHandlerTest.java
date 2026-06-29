package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DrawAndDiscardCardEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DrawAndDiscardCardEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Draws then sets up discard")
            void drawsThenDiscards() {
                Card card = createCard("Faithless Looting");
                DrawAndDiscardCardEffect effect = new DrawAndDiscardCardEffect(2, 1);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));
                gd.playerHands.get(player1Id).add(createCard("Mountain"));

                resolveEffect(gd, entry, effect);

                verify(drawService, times(2)).resolveDrawCard(gd, player1Id);
                assertThat(gd.discardCausedByOpponent).isFalse();
                verify(playerInputService).beginDiscardChoice(gd, player1Id);
            }
}
