package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.DealDamageToTargetCreatureEqualToControlledSubtypeCountEffectHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DealDamageToTargetCreatureEqualToControlledSubtypeCountEffectHandlerTest extends AbstractDamageHandlerTest {

    private DealDamageToTargetCreatureEqualToControlledSubtypeCountEffectHandler dealDamageToTargetCreatureEqualToControlledSubtypeCountHandler;

    @Override
    protected void setUpHandler() {
        dealDamageToTargetCreatureEqualToControlledSubtypeCountHandler = new DealDamageToTargetCreatureEqualToControlledSubtypeCountEffectHandler(damageSupport, gameQueryService, lifeSupport);
    }

    @Test
            @DisplayName("Deals damage equal to controlled subtype count â€” creature survives")
            void dealsDamageEqualToSubtypeCount() {
                Card spittingCard = createCard("Spitting Earth");
                Permanent angel = addPermanent(player2Id, createCreature("Serra Angel", 4, 4));
                StackEntry entry = createEntry(spittingCard, player1Id, angel.getId());
                DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect effect =
                        new DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect(CardSubtype.MOUNTAIN, false);

                stubDamagePreventable();
                stubDamageFromSourceNotPrevented();
                stubNoDamageMultiplier();
                stubCreatureDamageCore(angel, 4);
                stubNoKeywordsOnSource(entry);
                when(gameQueryService.countControlledSubtypePermanents(gd, player1Id, CardSubtype.MOUNTAIN)).thenReturn(3);
                when(gameQueryService.findPermanentById(gd, angel.getId())).thenReturn(angel);
                when(gameQueryService.isLethalDamage(3, 4, false)).thenReturn(false);

                dealDamageToTargetCreatureEqualToControlledSubtypeCountHandler.resolve(gd, entry, effect);

                assertThat(angel.getMarkedDamage()).isEqualTo(3);
                verify(permanentRemovalService, never()).removePermanentToGraveyard(any(), any());
                verify(triggerCollectionService).checkDealtDamageToCreatureTriggers(gd, angel, 3, player1Id);
            }

            @Test
            @DisplayName("Kills creature when damage equals toughness")
            void killsCreatureWhenDamageEqualsOrExceedsToughness() {
                Card spittingCard = createCard("Spitting Earth");
                Permanent bears = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
                StackEntry entry = createEntry(spittingCard, player1Id, bears.getId());
                DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect effect =
                        new DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect(CardSubtype.MOUNTAIN, false);

                stubDamagePreventable();
                stubDamageFromSourceNotPrevented();
                stubNoDamageMultiplier();
                stubCreatureDamageCore(bears, 2);
                stubNoKeywordsOnSource(entry);
                stubLethalDamage(true);
                when(gameQueryService.countControlledSubtypePermanents(gd, player1Id, CardSubtype.MOUNTAIN)).thenReturn(2);
                when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);
                when(gameQueryService.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).thenReturn(false);
                when(graveyardService.tryRegenerate(gd, bears)).thenReturn(false);

                dealDamageToTargetCreatureEqualToControlledSubtypeCountHandler.resolve(gd, entry, effect);

                assertThat(bears.getMarkedDamage()).isEqualTo(2);
                verify(triggerCollectionService).checkDealtDamageToCreatureTriggers(gd, bears, 2, player1Id);
            }

            @Test
            @DisplayName("Deals 0 damage when controller has no subtypes")
            void dealsZeroDamageWithNoSubtypes() {
                Card spittingCard = createCard("Spitting Earth");
                Permanent bears = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
                StackEntry entry = createEntry(spittingCard, player1Id, bears.getId());
                DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect effect =
                        new DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect(CardSubtype.MOUNTAIN, false);

                stubDamagePreventable();
                stubDamageFromSourceNotPrevented();
                stubNoDamageMultiplier();
                stubCreatureDamageCore(bears, 2);
                stubNoKeywordsOnSource(entry);
                when(gameQueryService.countControlledSubtypePermanents(gd, player1Id, CardSubtype.MOUNTAIN)).thenReturn(0);
                when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);
                when(gameQueryService.isLethalDamage(0, 2, false)).thenReturn(false);

                dealDamageToTargetCreatureEqualToControlledSubtypeCountHandler.resolve(gd, entry, effect);

                assertThat(bears.getMarkedDamage()).isEqualTo(0);
                verify(permanentRemovalService, never()).removePermanentToGraveyard(any(), any());
                verifyNoInteractions(triggerCollectionService);
            }
}
