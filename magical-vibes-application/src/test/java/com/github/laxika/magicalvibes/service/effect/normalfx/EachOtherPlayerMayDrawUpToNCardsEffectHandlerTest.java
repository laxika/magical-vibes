package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.EachOtherPlayerMayDrawUpToNCardsEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class EachOtherPlayerMayDrawUpToNCardsEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
    @DisplayName("Prompts each player other than the controller")
    void promptsOtherPlayer() {
        Card card = createCard("Indentured Djinn");
        EachOtherPlayerMayDrawUpToNCardsEffect effect = new EachOtherPlayerMayDrawUpToNCardsEffect(3);
        StackEntry entry = createEntry(card, player1Id, List.of(effect));

        resolveEffect(gd, entry, effect);

        assertThat(gd.pendingEachOtherPlayerDrawUpToQueue).containsExactly(player2Id);
        verify(interactionHandlerRegistry).begin(eq(gd),
                org.mockito.ArgumentMatchers.isA(PendingInteraction.XValueChoice.class));
    }

    @Test
    @DisplayName("Applies the other player's chosen draw amount without drawing for the controller")
    void appliesChosenDrawAmount() {
        Card card = createCard("Indentured Djinn");
        EachOtherPlayerMayDrawUpToNCardsEffect effect = new EachOtherPlayerMayDrawUpToNCardsEffect(3);
        StackEntry entry = createEntry(card, player1Id, List.of(effect));

        resolveEffect(gd, entry, effect);
        gd.chosenXValue = 2;
        resolveEffect(gd, entry, effect);

        verify(drawService, times(2)).resolveDrawCard(gd, player2Id);
        verify(drawService, never()).resolveDrawCard(gd, player1Id);
        assertThat(gd.pendingEachOtherPlayerDrawUpToQueue).isEmpty();
        assertThat(gd.chosenXValue).isNull();
    }
}
