package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerByHandSizeEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DealDamageToTargetPlayerByHandSizeEffectHandlerTest extends AbstractDamageHandlerTest {

    private DealDamageToTargetPlayerByHandSizeEffectHandler dealDamageToTargetPlayerByHandSizeHandler;

    @Override
    protected void setUpHandler() {
        dealDamageToTargetPlayerByHandSizeHandler = new DealDamageToTargetPlayerByHandSizeEffectHandler(damageSupport, gameQueryService, gameOutcomeService);
    }

    @Test
            @DisplayName("Deals damage equal to target player's hand size")
            void dealsDamageEqualToHandSize() {
                Card impactCard = createCard("Sudden Impact");
                StackEntry entry = createEntry(impactCard, player1Id, player2Id);

                // Give player2 a hand of 5 cards
                for (int i = 0; i < 5; i++) {
                    gd.playerHands.get(player2Id).add(createCreature("Bear " + i, 2, 2));
                }

                stubDamagePreventable();
                stubDamageFromSourceNotPrevented();
                stubNoDamageMultiplier();
                stubPlayerDamageCore(player2Id);
                stubNoInfectOnSource(entry);

                dealDamageToTargetPlayerByHandSizeHandler.resolve(gd, entry, new DealDamageToTargetPlayerByHandSizeEffect());

                assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(15);
                verify(triggerCollectionService).checkLifeLossTriggers(gd, player2Id, 5);
                verify(triggerCollectionService).checkDamageDealtToControllerTriggers(gd, player2Id, null, false);
                verify(triggerCollectionService).checkNoncombatDamageToOpponentTriggers(gd, player2Id);
            }

            @Test
            @DisplayName("Deals 0 damage when target has an empty hand")
            void dealsZeroDamageWhenEmptyHand() {
                Card impactCard = createCard("Sudden Impact");
                StackEntry entry = createEntry(impactCard, player1Id, player2Id);

                stubDamagePreventable();
                stubDamageFromSourceNotPrevented();
                stubNoDamageMultiplier();
                // dealDamageToPlayer is called with rawDamage=0, early-returns after applySourceRedirectShields
                when(damagePreventionService.isSourceDamagePreventedForPlayer(eq(gd), eq(player2Id), any())).thenReturn(false);
                when(damagePreventionService.applySourceRedirectShields(eq(gd), eq(player2Id), any(), anyInt())).thenAnswer(inv -> inv.getArgument(3));

                dealDamageToTargetPlayerByHandSizeHandler.resolve(gd, entry, new DealDamageToTargetPlayerByHandSizeEffect());

                assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(20);
                verifyNoInteractions(triggerCollectionService);
            }
}
