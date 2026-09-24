package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DrawCardForDamagedPlayerEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class DrawCardForDamagedPlayerEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
    @DisplayName("Draws cards for the combat-damaged player")
    void drawsForDamagedPlayer() {
        Card card = createCard("Black Widow, Intel Expert");
        DrawCardForDamagedPlayerEffect effect = new DrawCardForDamagedPlayerEffect(2);
        StackEntry entry = createTriggeredEntryWithTarget(card, player1Id, List.of(effect), player2Id,
                UUID.randomUUID());

        resolveEffect(gd, entry, effect);

        verify(drawService, times(2)).resolveDrawCard(gd, player2Id);
    }
}
