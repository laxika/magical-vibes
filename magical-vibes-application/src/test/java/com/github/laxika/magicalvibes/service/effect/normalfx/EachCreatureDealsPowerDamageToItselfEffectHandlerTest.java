package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.EachCreatureDealsPowerDamageToItselfEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

class EachCreatureDealsPowerDamageToItselfEffectHandlerTest extends AbstractDamageHandlerTest {

    private EachCreatureDealsPowerDamageToItselfEffectHandler handler;

    @Override
    protected void setUpHandler() {
        handler = new EachCreatureDealsPowerDamageToItselfEffectHandler(
                damageSupport, gameQueryService, gameLogService, gameOutcomeService);
    }

    @Test
    @DisplayName("Uses each creature's power and the creature itself as the damage source")
    void usesEachCreaturePowerAndSource() {
        Card spell = createCard("Wave of Reckoning");
        Permanent wall = addPermanent(player1Id, createCreature("Wall of Swords", 3, 5));
        Permanent spider = addPermanent(player2Id, createCreature("Giant Spider", 2, 4));
        StackEntry entry = createEntry(spell, player1Id, null);

        stubDamagePreventable();
        stubNoDamageMultiplier();
        stubCreatureSourceRedirects();
        when(gameQueryService.isCreature(eq(gd), any(Permanent.class))).thenReturn(true);
        when(gameQueryService.findPermanentById(gd, wall.getId())).thenReturn(wall);
        when(gameQueryService.findPermanentById(gd, spider.getId())).thenReturn(spider);
        when(gameQueryService.getPowerBasedDamage(gd, wall)).thenReturn(3);
        when(gameQueryService.getPowerBasedDamage(gd, spider)).thenReturn(2);
        when(gameQueryService.isPreventedFromDealingDamage(gd, wall)).thenReturn(false);
        when(gameQueryService.isPreventedFromDealingDamage(gd, spider)).thenReturn(false);
        when(gameQueryService.hasProtectionFromSource(gd, wall, wall)).thenReturn(false);
        when(gameQueryService.hasProtectionFromSource(gd, spider, spider)).thenReturn(false);
        lenient().when(gameQueryService.sourceHasKeyword(eq(gd), any(StackEntry.class), eq(wall), any()))
                .thenReturn(false);
        lenient().when(gameQueryService.sourceHasKeyword(eq(gd), any(StackEntry.class), eq(spider), any()))
                .thenReturn(false);
        stubCreatureDamageCore(wall, 5);
        stubCreatureDamageCore(spider, 4);

        handler.resolve(gd, entry, new EachCreatureDealsPowerDamageToItselfEffect());

        assertThat(wall.getMarkedDamage()).isEqualTo(3);
        assertThat(spider.getMarkedDamage()).isEqualTo(2);
    }
}
