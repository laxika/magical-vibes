package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DrawCardEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Draws the specified number of cards for the controller")
            void drawsSpecifiedAmount() {
                Card card = createCard("Divination");
                DrawCardEffect effect = new DrawCardEffect(3);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));

                resolveEffect(gd, entry, effect);

                verify(drawService, times(3)).resolveDrawCard(gd, player1Id);
            }

            @Test
            @DisplayName("Draws 1 card for single draw")
            void drawsSingleCard() {
                Card card = createCard("Opt");
                DrawCardEffect effect = new DrawCardEffect(1);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));

                resolveEffect(gd, entry, effect);

                verify(drawService, times(1)).resolveDrawCard(gd, player1Id);
            }
}
