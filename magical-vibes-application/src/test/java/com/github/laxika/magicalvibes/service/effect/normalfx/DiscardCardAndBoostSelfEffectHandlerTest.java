package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DiscardCardAndBoostSelfEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DiscardCardAndBoostSelfEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
    @DisplayName("Sets pending boost follow-up and begins discard")
    void setsBoostAndBeginsDiscard() {
        Card card = createCard("Furyblade Vampire");
        DiscardCardAndBoostSelfEffect effect = new DiscardCardAndBoostSelfEffect(3, 0);
        UUID sourcePermanentId = UUID.randomUUID();
        StackEntry entry = createTriggeredEntry(card, player1Id, List.of(effect), sourcePermanentId);
        gd.playerHands.get(player1Id).add(createCard("Mountain"));

        resolveEffect(gd, entry, effect);

        assertThat(gd.discardCausedByOpponent).isFalse();
        verify(playerInputService).beginDiscardChoice(eq(gd), eq(player1Id), anyInt(),
                argThat((DiscardFollowUp f) ->
                        sourcePermanentId.equals(f.boostPermanentId())
                                && f.boostPower() == 3
                                && f.boostToughness() == 0));
    }

    @Test
    @DisplayName("Does nothing when hand is empty")
    void doesNothingWhenHandEmpty() {
        Card card = createCard("Furyblade Vampire");
        DiscardCardAndBoostSelfEffect effect = new DiscardCardAndBoostSelfEffect(3, 0);
        StackEntry entry = createEntry(card, player1Id, List.of(effect));

        resolveEffect(gd, entry, effect);

        verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) ->
                logEntry.plainText().contains("no cards to discard")));
    }
}
