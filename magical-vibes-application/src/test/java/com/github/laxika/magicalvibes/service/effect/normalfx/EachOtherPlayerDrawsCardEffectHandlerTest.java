package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.EachOtherPlayerDrawsCardEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class EachOtherPlayerDrawsCardEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
    @DisplayName("Every player other than the controller draws the specified number of cards")
    void otherPlayersDraw() {
        Card card = createCard("Words of Wisdom");
        EachOtherPlayerDrawsCardEffect effect = new EachOtherPlayerDrawsCardEffect(2);
        StackEntry entry = createEntry(card, player1Id, List.of(effect));

        resolveEffect(gd, entry, effect);

        verify(drawService, times(2)).resolveDrawCard(gd, player2Id);
        verify(drawService, times(0)).resolveDrawCard(gd, player1Id);
    }
}
