package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.LookAtDefendingPlayerHandEffect;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.verify;

class LookAtDefendingPlayerHandEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
    void revealsTheAttackedPlayersHandToTheTriggerController() {
        Card card = createCard("Port Inspector");
        StackEntry entry = createTriggeredEntry(card, player1Id, List.of(new LookAtDefendingPlayerHandEffect()), null);
        entry.setAttackedTargetId(player2Id);

        resolveEffect(gd, entry, new LookAtDefendingPlayerHandEffect());

        verify(cardRevealService).lookAtHand(gd, player1Id, player2Id);
    }
}
