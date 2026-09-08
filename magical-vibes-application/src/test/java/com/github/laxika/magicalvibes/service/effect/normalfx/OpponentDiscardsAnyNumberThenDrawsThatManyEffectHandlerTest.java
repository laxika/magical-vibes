package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.OpponentDiscardsAnyNumberThenDrawsThatManyEffect;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OpponentDiscardsAnyNumberThenDrawsThatManyEffectHandlerTest
        extends AbstractPlayerInteractionHandlerTest {

    @Test
    void beginsTheOpponentNumberChoice() {
        Card card = createCard("Fervent Mastery");
        gd.playerHands.get(player2Id).addAll(List.of(createCard("A"), createCard("B")));
        when(gameQueryService.getOpponentId(gd, player1Id)).thenReturn(player2Id);
        OpponentDiscardsAnyNumberThenDrawsThatManyEffect effect =
                new OpponentDiscardsAnyNumberThenDrawsThatManyEffect();
        StackEntry entry = createEntry(card, player1Id, List.of(effect));

        resolveEffect(gd, entry, effect);

        verify(interactionHandlerRegistry).begin(eq(gd), argThat(interaction ->
                interaction instanceof PendingInteraction.XValueChoice choice
                        && choice.playerId().equals(player2Id)
                        && choice.maxValue() == 2));
    }

    @Test
    void startsTheOpponentsDiscardAndDrawFollowUp() {
        Card card = createCard("Fervent Mastery");
        gd.playerHands.get(player2Id).addAll(List.of(createCard("A"), createCard("B")));
        when(gameQueryService.getOpponentId(gd, player1Id)).thenReturn(player2Id);
        gd.chosenXValue = 2;
        OpponentDiscardsAnyNumberThenDrawsThatManyEffect effect =
                new OpponentDiscardsAnyNumberThenDrawsThatManyEffect();
        StackEntry entry = createEntry(card, player1Id, List.of(effect));

        resolveEffect(gd, entry, effect);

        assertThat(gd.chosenXValue).isNull();
        verify(playerInputService).beginDiscardChoice(eq(gd), eq(player2Id), eq(2),
                argThat((DiscardFollowUp followUp) -> followUp.rummageDrawCount() == 2));
        assertThat(gd.discardCausedByOpponent).isTrue();
    }
}
