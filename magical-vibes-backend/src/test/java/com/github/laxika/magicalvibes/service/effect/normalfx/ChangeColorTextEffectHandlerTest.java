package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.ChangeColorTextEffect;
import com.github.laxika.magicalvibes.networking.message.ChooseFromListMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ChangeColorTextEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Begins color choice when target permanent exists")
            void beginsColorChoice() {
                Card card = createCard("Trait Doctoring");
                ChangeColorTextEffect effect = new ChangeColorTextEffect();
                Permanent target = new Permanent(createCard("Grizzly Bears"));
                StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), target.getId());

                when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);

                resolveEffect(gd, entry, new ChangeColorTextEffect());

                verify(sessionManager).sendToPlayer(eq(player1Id), any(ChooseFromListMessage.class));
            }

            @Test
            @DisplayName("Does nothing when target permanent is gone")
            void targetGone() {
                Card card = createCard("Trait Doctoring");
                ChangeColorTextEffect effect = new ChangeColorTextEffect();
                UUID missingId = UUID.randomUUID();
                StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), missingId);

                when(gameQueryService.findPermanentById(gd, missingId)).thenReturn(null);

                resolveEffect(gd, entry, new ChangeColorTextEffect());

                verify(sessionManager, never()).sendToPlayer(any(), any(ChooseFromListMessage.class));
            }
}
