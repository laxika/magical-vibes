package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.EachOpponentDrawsCardEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.inOrder;

class EachOpponentDrawsCardEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
    @DisplayName("The opponents draw in APNAP order, not in seating order (CR 101.4)")
    void opponentsDrawInApnapOrder() {
        UUID player3Id = UUID.randomUUID();
        gd.playerIds.add(player3Id);
        gd.orderedPlayerIds.add(player3Id);
        gd.playerHands.put(player3Id, new ArrayList<>());
        gd.activePlayerId = player3Id;
        Card card = createCard("Wingbright Thief");
        EachOpponentDrawsCardEffect effect = new EachOpponentDrawsCardEffect(1);

        resolveEffect(gd, createEntry(card, player1Id, List.of(effect)), effect);

        var order = inOrder(drawService);
        order.verify(drawService).resolveDrawCards(gd, player3Id, 1);
        order.verify(drawService).resolveDrawCards(gd, player2Id, 1);
    }
}
