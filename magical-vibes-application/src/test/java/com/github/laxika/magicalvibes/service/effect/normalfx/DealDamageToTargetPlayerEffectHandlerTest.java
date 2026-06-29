package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.DealDamageToTargetPlayerEffectHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DealDamageToTargetPlayerEffectHandlerTest extends AbstractDamageHandlerTest {

    private DealDamageToTargetPlayerEffectHandler dealDamageToTargetPlayerHandler;

    @Override
    protected void setUpHandler() {
        dealDamageToTargetPlayerHandler = new DealDamageToTargetPlayerEffectHandler(damageSupport, gameQueryService, gameOutcomeService);
    }

    @Test
            @DisplayName("Deals damage to target player")
            void dealsDamageToTargetPlayer() {
                Card lavaAxeCard = createCard("Lava Axe");
                StackEntry entry = createEntry(lavaAxeCard, player1Id, player2Id);
                DealDamageToTargetPlayerEffect effect = new DealDamageToTargetPlayerEffect(5);

                stubDamagePreventable();
                stubDamageFromSourceNotPrevented();
                stubNoDamageMultiplier();
                stubPlayerDamageCore(player2Id);
                stubNoInfectOnSource(entry);

                dealDamageToTargetPlayerHandler.resolve(gd, entry, effect);

                assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(15);
                verify(triggerCollectionService).checkLifeLossTriggers(gd, player2Id, 5);
                verify(triggerCollectionService).checkDamageDealtToControllerTriggers(gd, player2Id, null, false);
                verify(triggerCollectionService).checkNoncombatDamageToOpponentTriggers(gd, player2Id);
            }

            @Test
            @DisplayName("Does nothing when target is not a player")
            void doesNothingWhenTargetNotPlayer() {
                Card lavaAxeCard = createCard("Lava Axe");
                UUID fakeId = UUID.randomUUID();
                StackEntry entry = createEntry(lavaAxeCard, player1Id, fakeId);
                DealDamageToTargetPlayerEffect effect = new DealDamageToTargetPlayerEffect(5);

                dealDamageToTargetPlayerHandler.resolve(gd, entry, effect);

                assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(20);
                verifyNoInteractions(triggerCollectionService);
            }
}
