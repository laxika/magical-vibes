package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DealDamageToAnyTargetEffectHandlerTest extends AbstractDamageHandlerTest {

    private DealDamageToAnyTargetEffectHandler dealDamageToAnyTargetHandler;

    @Override
    protected void setUpHandler() {
        dealDamageToAnyTargetHandler = new DealDamageToAnyTargetEffectHandler(damageSupport, gameQueryService, gameOutcomeService, amountEvaluationService);
    }

    @Test
            @DisplayName("Deals lethal damage to a creature and destroys it")
            void dealsLethalDamageToCreatureAndDestroysIt() {
                Card shockCard = createCard("Shock");
                Permanent bears = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
                StackEntry entry = createEntry(shockCard, player1Id, bears.getId());
                DealDamageToAnyTargetEffect effect = new DealDamageToAnyTargetEffect(2, false);

                stubDamagePreventable();
                stubDamageFromSourceNotPrevented();
                stubNoDamageMultiplier();
                stubCreatureDamageCore(bears, 2);
                stubNoKeywordsOnSource(entry);
                stubLethalDamage(true);
                when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);
                when(gameQueryService.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).thenReturn(false);
                when(graveyardService.tryRegenerate(gd, bears)).thenReturn(false);

                dealDamageToAnyTargetHandler.resolve(gd, entry, effect);

                assertThat(bears.getMarkedDamage()).isEqualTo(2);
                assertThat(gd.pendingLethalDamageDestructions).contains(bears);
                verify(triggerCollectionService).checkDealtDamageToCreatureTriggers(gd, bears, 2, player1Id);
            }

            @Test
            @DisplayName("Deals non-lethal damage to a creature and it survives")
            void dealsNonLethalDamageToCreature() {
                Card shockCard = createCard("Shock");
                Permanent angel = addPermanent(player2Id, createCreature("Serra Angel", 4, 4));
                StackEntry entry = createEntry(shockCard, player1Id, angel.getId());
                DealDamageToAnyTargetEffect effect = new DealDamageToAnyTargetEffect(2, false);

                stubDamagePreventable();
                stubDamageFromSourceNotPrevented();
                stubNoDamageMultiplier();
                stubCreatureDamageCore(angel, 4);
                stubNoKeywordsOnSource(entry);
                stubLethalDamage(false);
                when(gameQueryService.findPermanentById(gd, angel.getId())).thenReturn(angel);

                dealDamageToAnyTargetHandler.resolve(gd, entry, effect);

                assertThat(angel.getMarkedDamage()).isEqualTo(2);
                verify(permanentRemovalService, never()).removePermanentToGraveyard(any(), any());
                verify(triggerCollectionService).checkDealtDamageToCreatureTriggers(gd, angel, 2, player1Id);
            }

            @Test
            @DisplayName("Deals damage to a player and reduces their life total")
            void dealsDamageToPlayer() {
                Card shockCard = createCard("Shock");
                StackEntry entry = createEntry(shockCard, player1Id, player2Id);
                DealDamageToAnyTargetEffect effect = new DealDamageToAnyTargetEffect(2, false);

                stubDamagePreventable();
                stubDamageFromSourceNotPrevented();
                stubNoDamageMultiplier();
                stubPlayerDamageCore(player2Id);
                stubNoInfectOnSource(entry);

                dealDamageToAnyTargetHandler.resolve(gd, entry, effect);

                assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(18);
                verify(triggerCollectionService).checkLifeLossTriggers(gd, player2Id, 2);
                verify(triggerCollectionService).checkDamageDealtToControllerTriggers(gd, player2Id, null, false);
                verify(triggerCollectionService).checkNoncombatDamageToOpponentTriggers(gd, player2Id);
            }

            @Test
            @DisplayName("Does nothing when targetId is null")
            void doesNothingWhenTargetNull() {
                Card shockCard = createCard("Shock");
                StackEntry entry = createEntry(shockCard, player1Id, null);
                DealDamageToAnyTargetEffect effect = new DealDamageToAnyTargetEffect(2, false);

                dealDamageToAnyTargetHandler.resolve(gd, entry, effect);

                verify(gameQueryService, never()).applyDamageMultiplier(any(), anyInt(), any());
                verifyNoInteractions(triggerCollectionService);
            }

            @Test
            @DisplayName("XValue amount deals damage equal to the stack entry's X value")
            void xValueAmountDealsXDamageToCreature() {
                Card blazeCard = createCard("Blaze");
                Permanent bears = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
                StackEntry entry = createEntryWithXValue(blazeCard, player1Id, 3, bears.getId());
                DealDamageToAnyTargetEffect effect = new DealDamageToAnyTargetEffect(new XValue());

                stubDamagePreventable();
                stubDamageFromSourceNotPrevented();
                stubNoDamageMultiplier();
                stubCreatureDamageCore(bears, 2);
                stubNoKeywordsOnSource(entry);
                stubLethalDamage(true);
                when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);
                when(gameQueryService.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).thenReturn(false);
                when(graveyardService.tryRegenerate(gd, bears)).thenReturn(false);

                dealDamageToAnyTargetHandler.resolve(gd, entry, effect);

                assertThat(bears.getMarkedDamage()).isEqualTo(3);
                assertThat(gd.pendingLethalDamageDestructions).contains(bears);
                verify(triggerCollectionService).checkDealtDamageToCreatureTriggers(gd, bears, 3, player1Id);
            }

            @Test
            @DisplayName("XValue amount deals X damage to a player")
            void xValueAmountDealsXDamageToPlayer() {
                Card blazeCard = createCard("Blaze");
                StackEntry entry = createEntryWithXValue(blazeCard, player1Id, 5, player2Id);
                DealDamageToAnyTargetEffect effect = new DealDamageToAnyTargetEffect(new XValue());

                stubDamagePreventable();
                stubDamageFromSourceNotPrevented();
                stubNoDamageMultiplier();
                stubPlayerDamageCore(player2Id);
                stubNoInfectOnSource(entry);

                dealDamageToAnyTargetHandler.resolve(gd, entry, effect);

                assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(15);
                verify(triggerCollectionService).checkLifeLossTriggers(gd, player2Id, 5);
                verify(triggerCollectionService).checkDamageDealtToControllerTriggers(gd, player2Id, null, false);
                verify(triggerCollectionService).checkNoncombatDamageToOpponentTriggers(gd, player2Id);
            }

            @Test
            @DisplayName("Damage is logged via broadcast service")
            void damageIsLogged() {
                Card shockCard = createCard("Shock");
                Permanent bears = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
                StackEntry entry = createEntry(shockCard, player1Id, bears.getId());
                DealDamageToAnyTargetEffect effect = new DealDamageToAnyTargetEffect(2, false);

                stubDamagePreventable();
                stubDamageFromSourceNotPrevented();
                stubNoDamageMultiplier();
                stubCreatureDamageCore(bears, 2);
                stubNoKeywordsOnSource(entry);
                stubLethalDamage(true);
                when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);
                when(gameQueryService.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).thenReturn(false);
                when(graveyardService.tryRegenerate(gd, bears)).thenReturn(false);

                dealDamageToAnyTargetHandler.resolve(gd, entry, effect);

                verify(gameBroadcastService, atLeastOnce()).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("Shock") && msg.contains("2 damage") && msg.contains("Grizzly Bears")));
            }
}
