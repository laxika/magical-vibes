package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetAndGainLifeEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DealDamageToAnyTargetAndGainLifeEffectHandlerTest extends AbstractDamageHandlerTest {

    private DealDamageToAnyTargetAndGainLifeEffectHandler dealDamageToAnyTargetAndGainLifeHandler;

    @Override
    protected void setUpHandler() {
        dealDamageToAnyTargetAndGainLifeHandler = new DealDamageToAnyTargetAndGainLifeEffectHandler(damageSupport, gameQueryService, gameOutcomeService, lifeSupport);
    }

    @Test
            @DisplayName("Deals damage to target player and controller gains life")
            void dealsDamageAndGainsLife() {
                Card drainCard = createCard("Essence Drain");
                drainCard.setColor(CardColor.BLACK);
                StackEntry entry = createEntry(drainCard, player1Id, player2Id);
                DealDamageToAnyTargetAndGainLifeEffect effect = new DealDamageToAnyTargetAndGainLifeEffect(3, 3);

                stubDamagePreventable();
                stubDamageFromSourceNotPrevented();
                stubNoDamageMultiplier();
                stubPlayerDamageCore(player2Id);
                stubNoInfectOnSource(entry);

                dealDamageToAnyTargetAndGainLifeHandler.resolve(gd, entry, effect);

                assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(17);
                verify(lifeSupport).applyGainLife(gd, player1Id, 3);
                verify(triggerCollectionService).checkLifeLossTriggers(gd, player2Id, 3);
                verify(triggerCollectionService).checkDamageDealtToControllerTriggers(gd, player2Id, null, false);
                verify(triggerCollectionService).checkNoncombatDamageToOpponentTriggers(gd, player2Id);
            }

            @Test
            @DisplayName("Deals damage to creature and controller gains life")
            void dealsDamageToCreatureAndGainsLife() {
                Card drainCard = createCard("Essence Drain");
                drainCard.setColor(CardColor.BLACK);
                Permanent bears = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
                StackEntry entry = createEntry(drainCard, player1Id, bears.getId());
                DealDamageToAnyTargetAndGainLifeEffect effect = new DealDamageToAnyTargetAndGainLifeEffect(3, 3);

                stubDamagePreventable();
                stubDamageFromSourceNotPrevented();
                stubNoDamageMultiplier();
                stubCreatureDamageCore(bears, 2);
                stubNoKeywordsOnSource(entry);
                when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);

                dealDamageToAnyTargetAndGainLifeHandler.resolve(gd, entry, effect);

                // Lethal marked damage — the SBA check after resolution performs the destruction.
                assertThat(bears.getMarkedDamage()).isEqualTo(3);
                verify(lifeSupport).applyGainLife(gd, player1Id, 3);
                verify(triggerCollectionService).checkDealtDamageToCreatureTriggers(gd, bears, 3, player1Id);
            }
}
