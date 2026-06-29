package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.DealDamageToTargetCreatureEffectHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DealDamageToTargetCreatureEffectHandlerTest extends AbstractDamageHandlerTest {

    private DealDamageToTargetCreatureEffectHandler dealDamageToTargetCreatureHandler;

    @Override
    protected void setUpHandler() {
        dealDamageToTargetCreatureHandler = new DealDamageToTargetCreatureEffectHandler(damageSupport, gameQueryService);
    }

    @Test
            @DisplayName("Deals damage to a creature and destroys it")
            void dealsDamageToCreature() {
                Card burnCard = createCard("Burn the Impure");
                Permanent bears = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
                StackEntry entry = createEntry(burnCard, player1Id, bears.getId());
                DealDamageToTargetCreatureEffect effect = new DealDamageToTargetCreatureEffect(3, false);

                stubDamagePreventable();
                stubDamageFromSourceNotPrevented();
                stubNoDamageMultiplier();
                stubCreatureDamageCore(bears, 2);
                stubNoKeywordsOnSource(entry);
                stubLethalDamage(true);
                when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);
                when(gameQueryService.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).thenReturn(false);
                when(graveyardService.tryRegenerate(gd, bears)).thenReturn(false);

                dealDamageToTargetCreatureHandler.resolve(gd, entry, effect);

                assertThat(bears.getMarkedDamage()).isEqualTo(3);
                verify(triggerCollectionService).checkDealtDamageToCreatureTriggers(gd, bears, 3, player1Id);
            }

            @Test
            @DisplayName("Tracks creature in permanentsDealtDamageThisTurn when damage is dealt")
            void tracksPermanentDealtDamageThisTurn() {
                Card burnCard = createCard("Shock");
                Permanent bears = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
                StackEntry entry = createEntry(burnCard, player1Id, bears.getId());
                DealDamageToTargetCreatureEffect effect = new DealDamageToTargetCreatureEffect(1, false);

                stubDamagePreventable();
                stubDamageFromSourceNotPrevented();
                stubNoDamageMultiplier();
                stubCreatureDamageCore(bears, 2);
                stubNoKeywordsOnSource(entry);
                stubLethalDamage(false);
                when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);

                dealDamageToTargetCreatureHandler.resolve(gd, entry, effect);

                assertThat(gd.permanentsDealtDamageThisTurn).contains(bears.getId());
            }

            @Test
            @DisplayName("Does not track creature in permanentsDealtDamageThisTurn when damage is fully prevented")
            void doesNotTrackWhenDamageFullyPrevented() {
                Card burnCard = createCard("Shock");
                Permanent bears = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
                StackEntry entry = createEntry(burnCard, player1Id, bears.getId());
                DealDamageToTargetCreatureEffect effect = new DealDamageToTargetCreatureEffect(1, false);

                stubDamagePreventable();
                stubDamageFromSourceNotPrevented();
                stubNoDamageMultiplier();
                when(gameQueryService.findPermanentController(eq(gd), eq(bears.getId()))).thenReturn(player2Id);
                // Prevention shield reduces damage to 0
                when(damagePreventionService.applyCreaturePreventionShield(eq(gd), eq(bears), anyInt())).thenReturn(0);
                when(gameQueryService.getEffectiveToughness(gd, bears)).thenReturn(2);
                stubNoKeywordsOnSource(entry);
                when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);

                dealDamageToTargetCreatureHandler.resolve(gd, entry, effect);

                assertThat(gd.permanentsDealtDamageThisTurn).doesNotContain(bears.getId());
            }

            @Test
            @DisplayName("Multi-target: deals damage to each creature in targetIds")
            void multiTargetDealsDamageToEachCreature() {
                Card burnCard = createCard("Dual Shot");
                Permanent bear1 = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
                Permanent bear2 = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
                StackEntry entry = createMultiTargetEntry(burnCard, player1Id, List.of(bear1.getId(), bear2.getId()));
                DealDamageToTargetCreatureEffect effect = new DealDamageToTargetCreatureEffect(1, false);

                stubDamagePreventable();
                stubDamageFromSourceNotPrevented();
                stubNoDamageMultiplier();
                stubCreatureDamageCore(bear1, 2);
                stubCreatureDamageCore(bear2, 2);
                stubNoKeywordsOnSource(entry);
                stubLethalDamage(false);
                when(gameQueryService.findPermanentById(gd, bear1.getId())).thenReturn(bear1);
                when(gameQueryService.findPermanentById(gd, bear2.getId())).thenReturn(bear2);
                when(gameQueryService.hasProtectionFromSource(eq(gd), any(Permanent.class), any(Card.class))).thenReturn(false);

                dealDamageToTargetCreatureHandler.resolve(gd, entry, effect);

                assertThat(bear1.getMarkedDamage()).isEqualTo(1);
                assertThat(bear2.getMarkedDamage()).isEqualTo(1);
            }

            @Test
            @DisplayName("Multi-target: skips removed targets and damages remaining ones")
            void multiTargetSkipsRemovedTargets() {
                Card burnCard = createCard("Dual Shot");
                Permanent bear1 = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
                Permanent bear2 = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
                UUID removedId = bear1.getId();
                StackEntry entry = createMultiTargetEntry(burnCard, player1Id, List.of(removedId, bear2.getId()));
                DealDamageToTargetCreatureEffect effect = new DealDamageToTargetCreatureEffect(1, false);

                stubDamagePreventable();
                stubDamageFromSourceNotPrevented();
                stubNoDamageMultiplier();
                stubCreatureDamageCore(bear2, 2);
                stubNoKeywordsOnSource(entry);
                stubLethalDamage(false);
                // bear1 was removed from battlefield before resolution
                when(gameQueryService.findPermanentById(gd, removedId)).thenReturn(null);
                when(gameQueryService.findPermanentById(gd, bear2.getId())).thenReturn(bear2);
                when(gameQueryService.hasProtectionFromSource(eq(gd), any(Permanent.class), any(Card.class))).thenReturn(false);

                dealDamageToTargetCreatureHandler.resolve(gd, entry, effect);

                assertThat(bear2.getMarkedDamage()).isEqualTo(1);
            }

            @Test
            @DisplayName("Single targetIds entry with primary targetId uses single-target path")
            void singleTargetIdsWithPrimaryTargetUsesSingleTargetPath() {
                Card burnCard = createCard("Goblin Barrage");
                Permanent bears = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
                // Kicked spell: creature in targetId, player in targetIds (size 1)
                StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, burnCard, player1Id, burnCard.getName(),
                        List.of(), 0, bears.getId(), null, Map.of(), null, List.of(), List.of(player2Id));
                DealDamageToTargetCreatureEffect effect = new DealDamageToTargetCreatureEffect(4, false);

                stubDamagePreventable();
                stubDamageFromSourceNotPrevented();
                stubNoDamageMultiplier();
                stubCreatureDamageCore(bears, 2);
                stubNoKeywordsOnSource(entry);
                stubLethalDamage(true);
                when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);
                when(gameQueryService.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).thenReturn(false);
                when(graveyardService.tryRegenerate(gd, bears)).thenReturn(false);

                dealDamageToTargetCreatureHandler.resolve(gd, entry, effect);

                assertThat(bears.getMarkedDamage()).isEqualTo(4);
            }
}
