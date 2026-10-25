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
            @DisplayName("Loses the life through LifeSupport when no cards in hand")
            void losesLifeThroughLifeSupportWhenNoCards() {
                Card card = createCard("Rackling");
                LoseLifeUnlessDiscardEffect effect = new LoseLifeUnlessDiscardEffect(3);
                StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

                resolveEffect(gd, entry, effect);

                verify(lifeSupport).applyLifeLoss(gd, player2Id, 3, "Rackling");
                verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) ->
                        logEntry.plainText().contains("no cards to discard")));
                assertThat(gd.pendingMayAbilities).isEmpty();
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
