package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToAnyTargetAndGainXLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToAnyTargetAndGainXLifeEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DealXDamageToAnyTargetAndGainXLifeEffectHandlerTest extends AbstractDamageHandlerTest {

    private DealXDamageToAnyTargetAndGainXLifeEffectHandler dealXDamageToAnyTargetAndGainXLifeHandler;

    @Override
    protected void setUpHandler() {
        dealXDamageToAnyTargetAndGainXLifeHandler = new DealXDamageToAnyTargetAndGainXLifeEffectHandler(damageSupport, gameQueryService, gameOutcomeService, lifeSupport);
    }

    @Test
            @DisplayName("Deals X damage and gains X life")
            void dealsXDamageAndGainsXLife() {
                Card consumeCard = createCard("Consume Spirit");
                consumeCard.setColor(CardColor.BLACK);
                StackEntry entry = createEntryWithXValue(consumeCard, player1Id, 3, player2Id);

                stubDamagePreventable();
                stubDamageFromSourceNotPrevented();
                stubNoDamageMultiplier();
                stubPlayerDamageCore(player2Id);
                stubNoInfectOnSource(entry);

                dealXDamageToAnyTargetAndGainXLifeHandler.resolve(gd, entry, new DealXDamageToAnyTargetAndGainXLifeEffect());

                assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(17);
                verify(lifeSupport).applyGainLife(gd, player1Id, 3);
                verify(triggerCollectionService).checkLifeLossTriggers(gd, player2Id, 3);
                verify(triggerCollectionService).checkDamageDealtToControllerTriggers(gd, player2Id, null, false);
                verify(triggerCollectionService).checkNoncombatDamageToOpponentTriggers(gd, player2Id);
            }
}
