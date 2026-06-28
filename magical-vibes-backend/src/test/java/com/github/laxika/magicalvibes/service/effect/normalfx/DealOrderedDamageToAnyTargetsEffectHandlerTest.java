package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DealOrderedDamageToAnyTargetsEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.DealOrderedDamageToAnyTargetsEffectHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DealOrderedDamageToAnyTargetsEffectHandlerTest extends AbstractDamageHandlerTest {

    private DealOrderedDamageToAnyTargetsEffectHandler dealOrderedDamageToAnyTargetsHandler;

    @Override
    protected void setUpHandler() {
        dealOrderedDamageToAnyTargetsHandler = new DealOrderedDamageToAnyTargetsEffectHandler(damageSupport, gameQueryService, gameBroadcastService, gameOutcomeService);
    }

    @Test
            @DisplayName("Deals ordered damage to two creature targets")
            void dealsOrderedDamageToTwoCreatures() {
                Card arcCard = createCard("Arc Trail");
                Permanent bears = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
                Permanent elves = addPermanent(player2Id, createCreature("Llanowar Elves", 1, 1));
                StackEntry entry = createMultiTargetEntry(arcCard, player1Id, List.of(bears.getId(), elves.getId()));
                DealOrderedDamageToAnyTargetsEffect effect = new DealOrderedDamageToAnyTargetsEffect(List.of(2, 1));

                stubDamagePreventable();
                stubDamageFromSourceNotPrevented();
                stubNoDamageMultiplier();
                stubCreatureDamageCore(bears, 2);
                stubCreatureDamageCore(elves, 1);
                when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);
                when(gameQueryService.findPermanentById(gd, elves.getId())).thenReturn(elves);
                stubNoKeywordsOnSource(entry);
                stubLethalDamage(true);
                when(gameQueryService.hasKeyword(eq(gd), any(Permanent.class), eq(Keyword.INDESTRUCTIBLE))).thenReturn(false);
                when(graveyardService.tryRegenerate(eq(gd), any(Permanent.class))).thenReturn(false);

                dealOrderedDamageToAnyTargetsHandler.resolve(gd, entry, effect);

                assertThat(bears.getMarkedDamage()).isEqualTo(2);
                assertThat(elves.getMarkedDamage()).isEqualTo(1);
                verify(permanentRemovalService).removePermanentToGraveyard(gd, bears);
                verify(permanentRemovalService).removePermanentToGraveyard(gd, elves);
                verify(triggerCollectionService).checkDealtDamageToCreatureTriggers(gd, bears, 2, player1Id);
                verify(triggerCollectionService).checkDealtDamageToCreatureTriggers(gd, elves, 1, player1Id);
            }

            @Test
            @DisplayName("Deals damage to a creature and a player")
            void dealsDamageToCreatureAndPlayer() {
                Card arcCard = createCard("Arc Trail");
                Permanent bears = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
                StackEntry entry = createMultiTargetEntry(arcCard, player1Id, List.of(bears.getId(), player2Id));
                DealOrderedDamageToAnyTargetsEffect effect = new DealOrderedDamageToAnyTargetsEffect(List.of(2, 1));

                stubDamagePreventable();
                stubDamageFromSourceNotPrevented();
                stubNoDamageMultiplier();
                stubCreatureDamageCore(bears, 2);
                when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);
                stubNoKeywordsOnSource(entry);
                stubLethalDamage(true);
                when(gameQueryService.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).thenReturn(false);
                when(graveyardService.tryRegenerate(gd, bears)).thenReturn(false);
                stubPlayerDamageCore(player2Id);

                dealOrderedDamageToAnyTargetsHandler.resolve(gd, entry, effect);

                assertThat(bears.getMarkedDamage()).isEqualTo(2);
                assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(19);
                verify(triggerCollectionService).checkDealtDamageToCreatureTriggers(gd, bears, 2, player1Id);
                verify(triggerCollectionService).checkLifeLossTriggers(gd, player2Id, 1);
                verify(triggerCollectionService).checkDamageDealtToControllerTriggers(gd, player2Id, null, false);
                verify(triggerCollectionService).checkNoncombatDamageToOpponentTriggers(gd, player2Id);
            }
}
