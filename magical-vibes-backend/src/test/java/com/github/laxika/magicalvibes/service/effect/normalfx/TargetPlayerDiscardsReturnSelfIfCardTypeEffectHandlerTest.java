package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsReturnSelfIfCardTypeEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TargetPlayerDiscardsReturnSelfIfCardTypeEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Sets pending return when target has cards")
            void setsPendingReturnWhenTargetHasCards() {
                Card card = createCard("Ravenous Rats");
                TargetPlayerDiscardsReturnSelfIfCardTypeEffect effect = new TargetPlayerDiscardsReturnSelfIfCardTypeEffect(1, CardType.CREATURE);
                StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);
                gd.playerHands.get(player2Id).add(createCard("Mountain"));

                resolveEffect(gd, entry, effect);

                assertThat(gd.pendingReturnToHandOnDiscardType).isNotNull();
                assertThat(gd.pendingReturnToHandOnDiscardType.requiredType()).isEqualTo(CardType.CREATURE);
                assertThat(gd.discardCausedByOpponent).isTrue();
            }

            @Test
            @DisplayName("Does not set pending return when target hand is empty")
            void noPendingReturnWhenHandEmpty() {
                Card card = createCard("Ravenous Rats");
                TargetPlayerDiscardsReturnSelfIfCardTypeEffect effect = new TargetPlayerDiscardsReturnSelfIfCardTypeEffect(1, CardType.CREATURE);
                StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

                resolveEffect(gd, entry, effect);

                assertThat(gd.pendingReturnToHandOnDiscardType).isNull();
            }
}
