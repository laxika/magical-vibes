package com.github.laxika.magicalvibes.service.effect.normalfx;

import org.mockito.InOrder;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.EachOtherPlayerDrawsCardEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
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


    @Test
    @DisplayName("The other players draw in APNAP order, starting with the active player")
    void otherPlayersDrawInApnapOrder() {
        UUID player3Id = UUID.randomUUID();
        gd.playerIds.add(player3Id);
        gd.orderedPlayerIds.add(player3Id);
        gd.playerIdToName.put(player3Id, "Charlie");
        gd.activePlayerId = player3Id;
        Card card = createCard("Dawn's Truce");
        EachOtherPlayerDrawsCardEffect effect = new EachOtherPlayerDrawsCardEffect(1);
        StackEntry entry = createEntry(card, player1Id, List.of(effect));

        resolveEffect(gd, entry, effect);

        InOrder order = inOrder(drawService);
        order.verify(drawService).resolveDrawCards(gd, player3Id, 1);
        order.verify(drawService).resolveDrawCards(gd, player2Id, 1);
        verify(drawService, never()).resolveDrawCards(eq(gd), eq(player1Id), anyInt());
    }
}
