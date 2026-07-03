package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaWithInstantSorceryCopyEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AwardAnyColorManaWithInstantSorceryCopyEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Sends color choice and registers spell copy trigger")
            void sendsColorChoiceAndRegistersCopy() {
                Card card = createCard("Unexpected Windfall");
                AwardAnyColorManaWithInstantSorceryCopyEffect effect = new AwardAnyColorManaWithInstantSorceryCopyEffect(2);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));

                resolveEffect(gd, entry, effect);

                verify(interactionHandlerRegistry).begin(eq(gd), any(PendingInteraction.ColorChoice.class));
                assertThat(gd.pendingNextInstantSorceryCopyCount.get(player1Id)).isEqualTo(1);
            }
}
