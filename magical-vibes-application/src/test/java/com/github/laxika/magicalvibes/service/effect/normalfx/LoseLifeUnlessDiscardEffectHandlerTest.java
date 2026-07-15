package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessDiscardEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LoseLifeUnlessDiscardEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Auto-applies life loss when no cards in hand")
            void autoAppliesLifeLossWhenNoCards() {
                Card card = createCard("Rackling");
                LoseLifeUnlessDiscardEffect effect = new LoseLifeUnlessDiscardEffect(3);
                StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

                when(gameQueryService.canPlayerLifeChange(gd, player2Id)).thenReturn(true);

                resolveEffect(gd, entry, effect);

                assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(17);
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) ->
                        logEntry.plainText().contains("no cards to discard") && logEntry.plainText().contains("loses 3 life")));
            }

            @Test
            @DisplayName("No life loss when life can't change and no cards")
            void noLifeLossWhenLifeCantChange() {
                Card card = createCard("Rackling");
                LoseLifeUnlessDiscardEffect effect = new LoseLifeUnlessDiscardEffect(3);
                StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

                when(gameQueryService.canPlayerLifeChange(gd, player2Id)).thenReturn(false);

                resolveEffect(gd, entry, effect);

                assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(20);
            }

            @Test
            @DisplayName("Presents may ability when cards are available")
            void presentsMayAbilityWhenCardsAvailable() {
                Card card = createCard("Rackling");
                LoseLifeUnlessDiscardEffect effect = new LoseLifeUnlessDiscardEffect(3);
                StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);
                gd.playerHands.get(player2Id).add(createCard("Mountain"));

                resolveEffect(gd, entry, effect);

                assertThat(gd.pendingMayAbilities).isNotEmpty();
                assertThat(gd.pendingMayAbilities.getFirst().controllerId()).isEqualTo(player2Id);
            }
}
