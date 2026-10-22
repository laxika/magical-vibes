package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DrawCardForYouAndDamagedPlayerEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.inOrder;

class DrawCardForYouAndDamagedPlayerEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
    @DisplayName("The active player draws first even when the other player controls the ability")
    void activePlayerDrawsFirst() {
        gd.activePlayerId = player1Id;
        Card card = createCard("Xyris, the Writhing Storm");
        DrawCardForYouAndDamagedPlayerEffect effect = new DrawCardForYouAndDamagedPlayerEffect(2);
        StackEntry entry = createTriggeredEntryWithTarget(card, player2Id, List.of(effect), player1Id,
                UUID.randomUUID());

        resolveEffect(gd, entry, effect);

        InOrder order = inOrder(drawService);
        order.verify(drawService).resolveDrawCards(gd, player1Id, 2);
        order.verify(drawService).resolveDrawCards(gd, player2Id, 2);
    }
}
