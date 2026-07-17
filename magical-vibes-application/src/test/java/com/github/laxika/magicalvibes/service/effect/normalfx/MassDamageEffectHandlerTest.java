package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MassDamageEffectHandlerTest extends AbstractDamageHandlerTest {

    private MassDamageEffectHandler massDamageHandler;

    @Override
    protected void setUpHandler() {
        massDamageHandler = new MassDamageEffectHandler(damageSupport, gameQueryService, predicateEvaluationService, gameOutcomeService, amountEvaluationService);
    }

    @Test
            @DisplayName("Deals damage to all creatures — lethal amounts are marked for the SBA check")
            void damagesAllCreatures() {
                Card pyroCard = createCard("Pyroclasm");
                Permanent bears = addPermanent(player1Id, createCreature("Grizzly Bears", 2, 2));
                Permanent elves = addPermanent(player2Id, createCreature("Llanowar Elves", 1, 1));
                StackEntry entry = createEntry(pyroCard, player1Id, null);
                MassDamageEffect effect = new MassDamageEffect(2);

                stubDamagePreventable();
                stubDamageFromSourceNotPrevented();
                stubNoDamageMultiplier();
                when(gameQueryService.isCreature(eq(gd), any(Permanent.class))).thenReturn(true);
                // Inline creature stubs â€” controllers differ from the default player2Id
                when(gameQueryService.findPermanentController(eq(gd), eq(bears.getId()))).thenReturn(player1Id);
                when(gameQueryService.findPermanentController(eq(gd), eq(elves.getId()))).thenReturn(player2Id);
                when(damagePreventionService.applyCreaturePreventionShield(eq(gd), eq(bears), anyInt())).thenAnswer(inv -> inv.getArgument(2));
                when(damagePreventionService.applyCreaturePreventionShield(eq(gd), eq(elves), anyInt())).thenAnswer(inv -> inv.getArgument(2));
                stubNoKeywordsOnSource(entry);

                massDamageHandler.resolve(gd, entry, effect);

                // Lethal marked damage — the SBA check after resolution performs the destruction.
                assertThat(bears.getMarkedDamage()).isEqualTo(2);
                assertThat(elves.getMarkedDamage()).isEqualTo(2);
                verify(triggerCollectionService).checkDealtDamageToCreatureTriggers(gd, bears, 2, player1Id);
                verify(triggerCollectionService).checkDealtDamageToCreatureTriggers(gd, elves, 2, player1Id);
            }

            @Test
            @DisplayName("Does not kill creatures with toughness > damage")
            void doesNotKillHighToughnessCreatures() {
                Card pyroCard = createCard("Pyroclasm");
                Permanent angel = addPermanent(player1Id, createCreature("Serra Angel", 4, 4));
                StackEntry entry = createEntry(pyroCard, player1Id, null);
                MassDamageEffect effect = new MassDamageEffect(2);

                stubDamagePreventable();
                stubDamageFromSourceNotPrevented();
                stubNoDamageMultiplier();
                when(gameQueryService.isCreature(eq(gd), any(Permanent.class))).thenReturn(true);
                // Inline creature stubs â€” controller is player1Id, not the default player2Id
                when(gameQueryService.findPermanentController(eq(gd), eq(angel.getId()))).thenReturn(player1Id);
                when(damagePreventionService.applyCreaturePreventionShield(eq(gd), eq(angel), anyInt())).thenAnswer(inv -> inv.getArgument(2));
                stubNoKeywordsOnSource(entry);

                massDamageHandler.resolve(gd, entry, effect);

                assertThat(angel.getMarkedDamage()).isEqualTo(2);
                verify(permanentRemovalService, never()).removePermanentToGraveyard(any(), any());
                verify(triggerCollectionService).checkDealtDamageToCreatureTriggers(gd, angel, 2, player1Id);
            }

            @Test
            @DisplayName("Deals X damage to creatures matching filter and to all players when damagesPlayers is true")
            void hurricaneDealsXDamageToFilteredCreaturesAndPlayers() {
                Card hurricaneCard = createCard("Hurricane");
                hurricaneCard.setColor(CardColor.GREEN);
                Permanent angel = addPermanent(player2Id, createCreature("Serra Angel", 4, 4));
                Permanent bears = addPermanent(player1Id, createCreature("Grizzly Bears", 2, 2));
                StackEntry entry = createEntryWithXValue(hurricaneCard, player1Id, 4, null);
                MassDamageEffect effect = new MassDamageEffect(0, true, true, null);

                stubDamagePreventable();
                stubDamageFromSourceNotPrevented();
                stubNoDamageMultiplier();
                when(gameQueryService.isCreature(eq(gd), any(Permanent.class))).thenReturn(true);
                // Inline creature stubs â€” controllers differ from the default player2Id
                when(gameQueryService.findPermanentController(eq(gd), eq(angel.getId()))).thenReturn(player2Id);
                when(gameQueryService.findPermanentController(eq(gd), eq(bears.getId()))).thenReturn(player1Id);
                when(damagePreventionService.applyCreaturePreventionShield(eq(gd), eq(angel), anyInt())).thenAnswer(inv -> inv.getArgument(2));
                when(damagePreventionService.applyCreaturePreventionShield(eq(gd), eq(bears), anyInt())).thenAnswer(inv -> inv.getArgument(2));
                stubNoKeywordsOnSource(entry);
                stubPlayerDamageCore(player1Id);
                stubPlayerDamageCore(player2Id);

                massDamageHandler.resolve(gd, entry, effect);

                assertThat(gd.playerLifeTotals.get(player1Id)).isEqualTo(16);
                assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(16);
                verify(gameOutcomeService).checkWinCondition(gd);
                verify(triggerCollectionService).checkDealtDamageToCreatureTriggers(gd, angel, 4, player1Id);
                verify(triggerCollectionService).checkDealtDamageToCreatureTriggers(gd, bears, 4, player1Id);
                verify(triggerCollectionService).checkLifeLossTriggers(gd, player1Id, 4);
                verify(triggerCollectionService).checkLifeLossTriggers(gd, player2Id, 4);
                verify(triggerCollectionService).checkDamageDealtToControllerTriggers(gd, player1Id, null, false);
                verify(triggerCollectionService).checkDamageDealtToControllerTriggers(gd, player2Id, null, false);
                verify(triggerCollectionService).checkNoncombatDamageToOpponentTriggers(gd, player1Id);
                verify(triggerCollectionService).checkNoncombatDamageToOpponentTriggers(gd, player2Id);
            }
}
