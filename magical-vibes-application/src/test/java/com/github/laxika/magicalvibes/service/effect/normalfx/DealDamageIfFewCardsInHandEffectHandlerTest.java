package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DealDamageIfFewCardsInHandEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DealDamageIfFewCardsInHandEffectHandlerTest extends AbstractDamageHandlerTest {

    private DealDamageIfFewCardsInHandEffectHandler dealDamageIfFewCardsInHandHandler;

    @Override
    protected void setUpHandler() {
        dealDamageIfFewCardsInHandHandler = new DealDamageIfFewCardsInHandEffectHandler(damageSupport, gameQueryService, gameBroadcastService, gameOutcomeService);
    }

    @Test
            @DisplayName("Deals damage when opponent has cards <= maxCards")
            void dealsDamageWhenOpponentHasFewCards() {
                Card museCard = createCard("Lavaborn Muse");
                StackEntry entry = createEntry(museCard, player1Id, player2Id);
                DealDamageIfFewCardsInHandEffect effect = new DealDamageIfFewCardsInHandEffect(2, 3);

                // Player2 has 2 cards (at the threshold)
                gd.playerHands.get(player2Id).add(createCreature("Bear1", 2, 2));
                gd.playerHands.get(player2Id).add(createCreature("Bear2", 2, 2));

                stubDamagePreventable();
                stubDamageFromSourceNotPrevented();
                stubNoDamageMultiplier();
                stubPlayerDamageCore(player2Id);
                stubNoInfectOnSource(entry);

                dealDamageIfFewCardsInHandHandler.resolve(gd, entry, effect);

                assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(17);
                verify(triggerCollectionService).checkLifeLossTriggers(gd, player2Id, 3);
                verify(triggerCollectionService).checkDamageDealtToControllerTriggers(gd, player2Id, null, false);
                verify(triggerCollectionService).checkNoncombatDamageToOpponentTriggers(gd, player2Id);
            }

            @Test
            @DisplayName("Deals damage when opponent has 0 cards in hand")
            void dealsDamageWhenOpponentHasEmptyHand() {
                Card museCard = createCard("Lavaborn Muse");
                StackEntry entry = createEntry(museCard, player1Id, player2Id);
                DealDamageIfFewCardsInHandEffect effect = new DealDamageIfFewCardsInHandEffect(2, 3);

                stubDamagePreventable();
                stubDamageFromSourceNotPrevented();
                stubNoDamageMultiplier();
                stubPlayerDamageCore(player2Id);
                stubNoInfectOnSource(entry);

                dealDamageIfFewCardsInHandHandler.resolve(gd, entry, effect);

                assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(17);
                verify(triggerCollectionService).checkLifeLossTriggers(gd, player2Id, 3);
                verify(triggerCollectionService).checkDamageDealtToControllerTriggers(gd, player2Id, null, false);
                verify(triggerCollectionService).checkNoncombatDamageToOpponentTriggers(gd, player2Id);
            }

            @Test
            @DisplayName("Does nothing when opponent has more than maxCards")
            void doesNothingWhenOpponentHasTooManyCards() {
                Card museCard = createCard("Lavaborn Muse");
                StackEntry entry = createEntry(museCard, player1Id, player2Id);
                DealDamageIfFewCardsInHandEffect effect = new DealDamageIfFewCardsInHandEffect(2, 3);

                // Player2 has 3 cards (exceeds maxCards of 2)
                gd.playerHands.get(player2Id).add(createCreature("Bear1", 2, 2));
                gd.playerHands.get(player2Id).add(createCreature("Bear2", 2, 2));
                gd.playerHands.get(player2Id).add(createCreature("Bear3", 2, 2));

                dealDamageIfFewCardsInHandHandler.resolve(gd, entry, effect);

                assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(20);
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) ->
                        logEntry.plainText().contains("does nothing")));
                verifyNoInteractions(triggerCollectionService);
            }
}
