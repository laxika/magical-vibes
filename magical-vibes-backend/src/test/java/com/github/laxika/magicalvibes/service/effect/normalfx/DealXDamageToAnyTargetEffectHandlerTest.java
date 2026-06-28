package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.DealXDamageToAnyTargetEffectHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DealXDamageToAnyTargetEffectHandlerTest extends AbstractDamageHandlerTest {

    private DealXDamageToAnyTargetEffectHandler dealXDamageToAnyTargetHandler;

    @Override
    protected void setUpHandler() {
        dealXDamageToAnyTargetHandler = new DealXDamageToAnyTargetEffectHandler(damageSupport, gameQueryService, gameOutcomeService);
    }

    @Test
            @DisplayName("Deals X damage to a creature and destroys it")
            void dealsXDamageToCreature() {
                Card blazeCard = createCard("Blaze");
                Permanent bears = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
                StackEntry entry = createEntryWithXValue(blazeCard, player1Id, 3, bears.getId());
                DealXDamageToAnyTargetEffect effect = new DealXDamageToAnyTargetEffect(false);

                stubDamagePreventable();
                stubDamageFromSourceNotPrevented();
                stubNoDamageMultiplier();
                stubCreatureDamageCore(bears, 2);
                stubNoKeywordsOnSource(entry);
                stubLethalDamage(true);
                when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);
                when(gameQueryService.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).thenReturn(false);
                when(graveyardService.tryRegenerate(gd, bears)).thenReturn(false);

                dealXDamageToAnyTargetHandler.resolve(gd, entry, effect);

                assertThat(bears.getMarkedDamage()).isEqualTo(3);
                assertThat(gd.pendingLethalDamageDestructions).contains(bears);
                verify(triggerCollectionService).checkDealtDamageToCreatureTriggers(gd, bears, 3, player1Id);
            }

            @Test
            @DisplayName("Deals X damage to a player")
            void dealsXDamageToPlayer() {
                Card blazeCard = createCard("Blaze");
                StackEntry entry = createEntryWithXValue(blazeCard, player1Id, 5, player2Id);
                DealXDamageToAnyTargetEffect effect = new DealXDamageToAnyTargetEffect(false);

                stubDamagePreventable();
                stubDamageFromSourceNotPrevented();
                stubNoDamageMultiplier();
                stubPlayerDamageCore(player2Id);
                stubNoInfectOnSource(entry);

                dealXDamageToAnyTargetHandler.resolve(gd, entry, effect);

                assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(15);
                verify(triggerCollectionService).checkLifeLossTriggers(gd, player2Id, 5);
                verify(triggerCollectionService).checkDamageDealtToControllerTriggers(gd, player2Id, null, false);
                verify(triggerCollectionService).checkNoncombatDamageToOpponentTriggers(gd, player2Id);
            }
}
