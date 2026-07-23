package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit coverage for the merged {@link DealDividedDamageEffectHandler} (the collapsed divided-damage
 * family). Every {@link com.github.laxika.magicalvibes.model.effect.DivisionMode} is exercised here;
 * the behavioural card tests (Ignite Disorder, Fight with Fire, Hail of Arrows, Fireball, Cone of
 * Flame, Arc Trail, Huatli, Inferno Titan, Bogardan Hellkite) prove the end-to-end routing.
 */
class DealDividedDamageEffectHandlerTest extends AbstractDamageHandlerTest {

    private DealDividedDamageEffectHandler handler;

    @Override
    protected void setUpHandler() {
        handler = new DealDividedDamageEffectHandler(damageSupport, gameQueryService, gameBroadcastService, gameOutcomeService);
    }

    private StackEntry chosenEntry(Card card, UUID controllerId, int xValue, Map<UUID, Integer> assignments) {
        return new StackEntry(StackEntryType.SORCERY_SPELL, card, controllerId, card.getName(),
                List.of(), xValue, null, assignments);
    }

    private StackEntry evenEntry(Card card, UUID controllerId, int xValue, List<UUID> targetIds) {
        return new StackEntry(StackEntryType.SORCERY_SPELL, card, controllerId, card.getName(), List.of(), xValue, targetIds);
    }

    @Nested
    @DisplayName("ORDERED mode")
    class Ordered {

        @Test
        @DisplayName("Deals ordered damage to two creature targets")
        void dealsOrderedDamageToTwoCreatures() {
            Card arcCard = createCard("Arc Trail");
            Permanent bears = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
            Permanent elves = addPermanent(player2Id, createCreature("Llanowar Elves", 1, 1));
            StackEntry entry = createMultiTargetEntry(arcCard, player1Id, List.of(bears.getId(), elves.getId()));
            DealDividedDamageEffect effect = DealDividedDamageEffect.ordered(List.of(2, 1));

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            stubCreatureDamageCore(bears, 2);
            stubCreatureDamageCore(elves, 1);
            when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);
            when(gameQueryService.findPermanentById(gd, elves.getId())).thenReturn(elves);
            stubNoKeywordsOnSource(entry);

            handler.resolve(gd, entry, effect);

            // Lethal marked damage — the SBA check after resolution performs the destruction.
            assertThat(bears.getMarkedDamage()).isEqualTo(2);
            assertThat(elves.getMarkedDamage()).isEqualTo(1);
            verify(triggerCollectionService).checkDealtDamageToCreatureTriggers(gd, bears, 2, player1Id);
            verify(triggerCollectionService).checkDealtDamageToCreatureTriggers(gd, elves, 1, player1Id);
        }

        @Test
        @DisplayName("Deals damage to a creature and a player")
        void dealsDamageToCreatureAndPlayer() {
            Card arcCard = createCard("Arc Trail");
            Permanent bears = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
            StackEntry entry = createMultiTargetEntry(arcCard, player1Id, List.of(bears.getId(), player2Id));
            DealDividedDamageEffect effect = DealDividedDamageEffect.ordered(List.of(2, 1));

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            stubCreatureDamageCore(bears, 2);
            when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);
            stubNoKeywordsOnSource(entry);
            stubPlayerDamageCore(player2Id);

            handler.resolve(gd, entry, effect);

            assertThat(bears.getMarkedDamage()).isEqualTo(2);
            assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(19);
            verify(triggerCollectionService).checkDealtDamageToCreatureTriggers(gd, bears, 2, player1Id);
            verify(triggerCollectionService).checkLifeLossTriggers(gd, player2Id, 1);
            verify(triggerCollectionService).checkDamageDealtToControllerTriggers(gd, player2Id, null, false);
            verify(triggerCollectionService).checkNoncombatDamageToOpponentTriggers(gd, player2Id);
        }

        @Test
        @DisplayName("Keeps ordered damage amounts tied to original target positions")
        void keepsAmountsTiedToOriginalTargetPositions() {
            Card arcCard = createCard("Arc Trail");
            UUID removedTargetId = UUID.randomUUID();
            StackEntry entry = createMultiTargetEntry(
                    arcCard, player1Id, List.of(removedTargetId, player2Id));
            entry.markTargetIllegal(0);
            DealDividedDamageEffect effect = DealDividedDamageEffect.ordered(List.of(2, 1));

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            stubNoKeywordsOnSource(entry);
            stubPlayerDamageCore(player2Id);

            handler.resolve(gd, entry, effect);

            assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(19);
            verify(triggerCollectionService).checkLifeLossTriggers(gd, player2Id, 1);
        }
    }

    @Nested
    @DisplayName("EVEN mode")
    class Even {

        @Test
        @DisplayName("Splits X evenly (rounded down) among targets")
        void splitsEvenly() {
            Card fireball = createCard("Fireball");
            Permanent bears = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
            Permanent elves = addPermanent(player2Id, createCreature("Llanowar Elves", 1, 1));
            StackEntry entry = evenEntry(fireball, player1Id, 7, List.of(bears.getId(), elves.getId()));
            DealDividedDamageEffect effect = DealDividedDamageEffect.xDividedEvenly();

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            stubCreatureDamageCore(bears, 2);
            stubCreatureDamageCore(elves, 1);
            when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);
            when(gameQueryService.findPermanentById(gd, elves.getId())).thenReturn(elves);
            stubNoKeywordsOnSource(entry);

            handler.resolve(gd, entry, effect);

            // floor(7 / 2) = 3 to each
            assertThat(bears.getMarkedDamage()).isEqualTo(3);
            assertThat(elves.getMarkedDamage()).isEqualTo(3);
        }

        @Test
        @DisplayName("Uses the declared target count when one target becomes illegal")
        void usesDeclaredTargetCountWhenOneTargetBecomesIllegal() {
            Card fireball = createCard("Fireball");
            UUID removedTargetId = UUID.randomUUID();
            StackEntry entry = evenEntry(
                    fireball, player1Id, 7, List.of(removedTargetId, player2Id));
            entry.markTargetIllegal(0);
            DealDividedDamageEffect effect = DealDividedDamageEffect.xDividedEvenly();

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            stubNoKeywordsOnSource(entry);
            stubPlayerDamageCore(player2Id);

            handler.resolve(gd, entry, effect);

            assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(17);
            verify(triggerCollectionService).checkLifeLossTriggers(gd, player2Id, 3);
        }
    }

    @Nested
    @DisplayName("CHOSEN mode")
    class Chosen {

        @Test
        @DisplayName("Deals announced amounts and applies the can't-block rider")
        void dealsChosenWithCantBlockRider() {
            Card huatli = createCard("Huatli, Warrior Poet");
            Permanent bears = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
            Permanent elves = addPermanent(player2Id, createCreature("Llanowar Elves", 1, 1));
            StackEntry entry = chosenEntry(huatli, player1Id, 3, Map.of(bears.getId(), 2, elves.getId(), 1));
            DealDividedDamageEffect effect = DealDividedDamageEffect.xAmongTargetCreaturesCantBlock();

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            stubCreatureDamageCore(bears, 2);
            stubCreatureDamageCore(elves, 1);
            when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);
            when(gameQueryService.findPermanentById(gd, elves.getId())).thenReturn(elves);
            stubNoKeywordsOnSource(entry);

            handler.resolve(gd, entry, effect);

            assertThat(bears.getMarkedDamage()).isEqualTo(2);
            assertThat(elves.getMarkedDamage()).isEqualTo(1);
            assertThat(bears.isCantBlockThisTurn()).isTrue();
            assertThat(elves.isCantBlockThisTurn()).isTrue();
        }
    }
}
