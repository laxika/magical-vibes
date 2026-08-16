package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsUpToThenDrawsThatManyEffect;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

class TargetPlayerDiscardsUpToThenDrawsThatManyEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
    void beginsTargetPlayersXValueChoiceCappedByHandSize() {
        Card card = createCard("Mishra's Command");
        gd.playerHands.get(player2Id).addAll(List.of(createCard("Mountain"), createCard("Forest")));
        TargetPlayerDiscardsUpToThenDrawsThatManyEffect effect =
                new TargetPlayerDiscardsUpToThenDrawsThatManyEffect(new XValue());
        StackEntry entry = createEntryWithXValueAndTarget(card, player1Id, List.of(effect), 3, player2Id);

        resolveEffect(gd, entry, effect);

        verify(interactionHandlerRegistry).begin(eq(gd), argThat(i ->
                i instanceof PendingInteraction.XValueChoice x
                        && x.playerId().equals(player2Id)
                        && x.maxValue() == 2));
    }

    @Test
    void reentryBeginsTargetPlayersDiscardAndDrawFollowUp() {
        Card card = createCard("Mishra's Command");
        gd.playerHands.get(player2Id).addAll(List.of(createCard("Mountain"), createCard("Forest")));
        TargetPlayerDiscardsUpToThenDrawsThatManyEffect effect =
                new TargetPlayerDiscardsUpToThenDrawsThatManyEffect(3);
        StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);
        gd.chosenXValue = 2;

        resolveEffect(gd, entry, effect);

        assertThat(gd.chosenXValue).isNull();
        verify(playerInputService).beginDiscardChoice(eq(gd), eq(player2Id), eq(2),
                argThat(followUp -> followUp.rummageDrawCount() == 2));
        assertThat(gd.discardCausedByOpponent).isTrue();
    }
}
