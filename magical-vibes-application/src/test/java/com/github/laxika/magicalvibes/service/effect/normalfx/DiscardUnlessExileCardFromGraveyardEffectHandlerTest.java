package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DiscardUnlessExileCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;

class DiscardUnlessExileCardFromGraveyardEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Forces discard when no matching graveyard cards")
            void forcesDiscardWhenNoMatch() {
                Card card = createCard("Rotting Fensnake");
                CardPredicate predicate = new CardNamedPredicate("Test Filter");
                DiscardUnlessExileCardFromGraveyardEffect effect = new DiscardUnlessExileCardFromGraveyardEffect(predicate);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));
                gd.playerHands.get(player1Id).add(createCard("Mountain"));

                // No matching cards in graveyard
                resolveEffect(gd, entry, effect);

                assertThat(gd.discardCausedByOpponent).isFalse();
                verify(playerInputService).beginDiscardChoice(eq(gd), eq(player1Id));
            }

            @Test
            @DisplayName("Offers may ability when matching graveyard cards exist")
            void offersMayAbilityWhenMatchExists() {
                Card card = createCard("Rotting Fensnake");
                CardPredicate predicate = new CardNamedPredicate("Test Filter");
                DiscardUnlessExileCardFromGraveyardEffect effect = new DiscardUnlessExileCardFromGraveyardEffect(predicate);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));
                Card graveyardCard = createCard("Zombie");
                gd.playerGraveyards.get(player1Id).add(graveyardCard);

                when(predicateEvaluationService.matchesCardPredicate(eq(graveyardCard), eq(predicate), any())).thenReturn(true);

                resolveEffect(gd, entry, effect);

                assertThat(gd.pendingMayAbilities).isNotEmpty();
                assertThat(gd.pendingMayAbilities.getFirst().controllerId()).isEqualTo(player1Id);
            }
}
