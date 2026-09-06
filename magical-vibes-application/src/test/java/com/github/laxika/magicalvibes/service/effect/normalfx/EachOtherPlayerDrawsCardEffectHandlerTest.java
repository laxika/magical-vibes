package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.EachOtherPlayerDrawsCardEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class EachOtherPlayerDrawsCardEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
    @DisplayName("Every player other than the controller draws the specified number of cards")
    void otherPlayersDraw() {
        Card card = createCard("Words of Wisdom");
        EachOtherPlayerDrawsCardEffect effect = new EachOtherPlayerDrawsCardEffect(2);
        StackEntry entry = createEntry(card, player1Id, List.of(effect));

        resolveEffect(gd, entry, effect);

        verify(drawService).resolveDrawCards(gd, player2Id, 2);
        verify(drawService, never()).resolveDrawCards(eq(gd), eq(player1Id), anyInt());
    }
}
