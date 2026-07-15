package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.GrantPermanentNoMaxHandSizeEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GrantPermanentNoMaxHandSizeEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Adds player to no max hand size set")
            void addsPlayerToSet() {
                Card card = createCard("Spellbook");
                GrantPermanentNoMaxHandSizeEffect effect = new GrantPermanentNoMaxHandSizeEffect();
                StackEntry entry = createEntry(card, player1Id, List.of(effect));

                resolveEffect(gd, entry, new GrantPermanentNoMaxHandSizeEffect());

                assertThat(gd.playersWithNoMaximumHandSize).contains(player1Id);
            }

            @Test
            @DisplayName("Logs the no max hand size grant")
            void logsGrant() {
                Card card = createCard("Spellbook");
                GrantPermanentNoMaxHandSizeEffect effect = new GrantPermanentNoMaxHandSizeEffect();
                StackEntry entry = createEntry(card, player1Id, List.of(effect));

                resolveEffect(gd, entry, new GrantPermanentNoMaxHandSizeEffect());

                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) ->
                        logEntry.plainText().contains("no maximum hand size")));
            }
}
