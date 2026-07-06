package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DealDamageToControllerEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DealDamageToControllerEffectHandlerTest extends AbstractDamageHandlerTest {

    private DealDamageToControllerEffectHandler dealDamageToControllerHandler;

    @Override
    protected void setUpHandler() {
        dealDamageToControllerHandler = new DealDamageToControllerEffectHandler(damageSupport, gameQueryService, gameBroadcastService, gameOutcomeService);
    }

    @Test
            @DisplayName("Deals damage to the controller of the ability")
            void dealsDamageToController() {
                Card artilleryCard = createCard("Orcish Artillery");
                StackEntry entry = createEntry(artilleryCard, player1Id, null);
                DealDamageToControllerEffect effect = new DealDamageToControllerEffect(3);

                stubNoDamageMultiplier();
                stubDamageFromSourceNotPrevented();
                stubPlayerDamageCore(player1Id);
                stubNoInfectOnSource(entry);

                dealDamageToControllerHandler.resolve(gd, entry, effect);

                assertThat(gd.playerLifeTotals.get(player1Id)).isEqualTo(17);
                verify(triggerCollectionService).checkLifeLossTriggers(gd, player1Id, 3);
                verify(triggerCollectionService).checkDamageDealtToControllerTriggers(gd, player1Id, null, false);
                verify(triggerCollectionService).checkNoncombatDamageToOpponentTriggers(gd, player1Id);
            }
}
