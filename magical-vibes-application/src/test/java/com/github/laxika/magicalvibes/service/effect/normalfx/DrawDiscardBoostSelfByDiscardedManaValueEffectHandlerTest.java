package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardBoostSelfByDiscardedManaValueEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DrawDiscardBoostSelfByDiscardedManaValueEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
    @DisplayName("Draws a card, sets pending boost, then begins discard")
    void drawsAndSetsBoost() {
        Card card = createCard("Spellbound Dragon");
        DrawDiscardBoostSelfByDiscardedManaValueEffect effect = new DrawDiscardBoostSelfByDiscardedManaValueEffect();
        UUID sourcePermanentId = UUID.randomUUID();
        StackEntry entry = createTriggeredEntry(card, player1Id, List.of(effect), sourcePermanentId);
        gd.playerHands.get(player1Id).add(createCard("Mountain"));

        resolveEffect(gd, entry, effect);

        verify(drawService).resolveDrawCard(gd, player1Id);
        assertThat(gd.pendingBoostSourceByDiscardedManaValue).isNotNull();
        assertThat(gd.pendingBoostSourceByDiscardedManaValue.sourcePermanentId()).isEqualTo(sourcePermanentId);
        assertThat(gd.discardCausedByOpponent).isFalse();
        verify(playerInputService).beginDiscardChoice(eq(gd), eq(player1Id), anyInt(),
                any(DiscardFollowUp.class));
    }

    @Test
    @DisplayName("Does not set boost when source permanent ID is null")
    void noBoostWhenNullSourcePermanent() {
        Card card = createCard("Spellbound Dragon");
        DrawDiscardBoostSelfByDiscardedManaValueEffect effect = new DrawDiscardBoostSelfByDiscardedManaValueEffect();
        StackEntry entry = createEntry(card, player1Id, List.of(effect));
        gd.playerHands.get(player1Id).add(createCard("Mountain"));

        resolveEffect(gd, entry, effect);

        verify(drawService).resolveDrawCard(gd, player1Id);
        assertThat(gd.pendingBoostSourceByDiscardedManaValue).isNull();
    }
}
