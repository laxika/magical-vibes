package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.FirstTargetDealsPowerDamageToSecondTargetEffect;
import com.github.laxika.magicalvibes.model.effect.FirstTargetDealsPowerDamageToSecondTargetEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class FirstTargetDealsPowerDamageToSecondTargetEffectHandlerTest extends AbstractDamageHandlerTest {

    private FirstTargetDealsPowerDamageToSecondTargetEffectHandler firstTargetDealsPowerDamageToSecondTargetHandler;

    @Override
    protected void setUpHandler() {
        firstTargetDealsPowerDamageToSecondTargetHandler = new FirstTargetDealsPowerDamageToSecondTargetEffectHandler(damageSupport, gameQueryService, gameBroadcastService);
    }

    @Test
            @DisplayName("Source creature deals its power as damage to target â€” target survives")
            void sourceDealsItsPowerAsDamageToTarget() {
                Card wingCard = createCard("Wing Puncture");
                wingCard.setColor(CardColor.GREEN);
                Permanent bears = addPermanent(player1Id, createCreature("Grizzly Bears", 2, 2));
                Permanent angel = addPermanent(player2Id, createCreature("Serra Angel", 4, 4));
                StackEntry entry = createMultiTargetEntry(wingCard, player1Id, List.of(bears.getId(), angel.getId()));

                stubDamagePreventable();
                stubNoDamageMultiplier();
                stubCreatureDamageCore(angel, 4);
                stubCreatureSourceRedirects();
                when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);
                when(gameQueryService.findPermanentById(gd, angel.getId())).thenReturn(angel);
                when(gameQueryService.getPowerBasedDamage(gd, bears)).thenReturn(2);
                when(gameQueryService.isPreventedFromDealingDamage(gd, bears)).thenReturn(false);
                when(gameQueryService.hasProtectionFromSource(eq(gd), eq(angel), any(Permanent.class))).thenReturn(false);
                // Stub controller for the biting creature (used for trigger sourceControllerId)
                when(gameQueryService.findPermanentController(eq(gd), eq(bears.getId()))).thenReturn(player1Id);
                stubNoKeywordsOnSourceWithDamageSource(entry, bears);
                when(gameQueryService.isLethalDamage(2, 4, false)).thenReturn(false);

                firstTargetDealsPowerDamageToSecondTargetHandler.resolve(gd, entry, new FirstTargetDealsPowerDamageToSecondTargetEffect());

                assertThat(angel.getMarkedDamage()).isEqualTo(2);
                verify(permanentRemovalService, never()).removePermanentToGraveyard(any(), any());
                verify(triggerCollectionService).checkDealtDamageToCreatureTriggers(gd, angel, 2, player1Id);
            }

            @Test
            @DisplayName("Kills target when source power >= target toughness")
            void killsTargetWhenPowerIsLethal() {
                Card wingCard = createCard("Wing Puncture");
                wingCard.setColor(CardColor.GREEN);
                Permanent myAngel = addPermanent(player1Id, createCreature("Serra Angel", 4, 4));
                Permanent theirAngel = addPermanent(player2Id, createCreature("Serra Angel", 4, 4));
                StackEntry entry = createMultiTargetEntry(wingCard, player1Id, List.of(myAngel.getId(), theirAngel.getId()));

                stubDamagePreventable();
                stubNoDamageMultiplier();
                stubCreatureDamageCore(theirAngel, 4);
                stubCreatureSourceRedirects();
                when(gameQueryService.findPermanentById(gd, myAngel.getId())).thenReturn(myAngel);
                when(gameQueryService.findPermanentById(gd, theirAngel.getId())).thenReturn(theirAngel);
                when(gameQueryService.getPowerBasedDamage(gd, myAngel)).thenReturn(4);
                when(gameQueryService.isPreventedFromDealingDamage(gd, myAngel)).thenReturn(false);
                when(gameQueryService.hasProtectionFromSource(eq(gd), eq(theirAngel), any(Permanent.class))).thenReturn(false);
                // Stub controller for the biting creature (used for trigger sourceControllerId)
                when(gameQueryService.findPermanentController(eq(gd), eq(myAngel.getId()))).thenReturn(player1Id);
                stubNoKeywordsOnSourceWithDamageSource(entry, myAngel);
                when(gameQueryService.isLethalDamage(4, 4, false)).thenReturn(true);
                when(gameQueryService.hasKeyword(gd, theirAngel, Keyword.INDESTRUCTIBLE)).thenReturn(false);
                when(graveyardService.tryRegenerate(gd, theirAngel)).thenReturn(false);

                firstTargetDealsPowerDamageToSecondTargetHandler.resolve(gd, entry, new FirstTargetDealsPowerDamageToSecondTargetEffect());

                assertThat(theirAngel.getMarkedDamage()).isEqualTo(4);
                verify(triggerCollectionService).checkDealtDamageToCreatureTriggers(gd, theirAngel, 4, player1Id);
            }
}
