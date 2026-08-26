package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.t.TerrificTeamUp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.TargetCreaturesDealPowerDamageToTargetEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class TargetCreaturesDealPowerDamageToTargetEffectHandlerTest extends AbstractDamageHandlerTest {

    private TargetCreaturesDealPowerDamageToTargetEffectHandler handler;

    @Override
    protected void setUpHandler() {
        handler = new TargetCreaturesDealPowerDamageToTargetEffectHandler(damageSupport, gameQueryService, gameLogService);
    }

    @Test
    @DisplayName("Each selected source creature deals its own power to the selected victim")
    void eachSelectedSourceDealsItsPower() {
        Card card = new TerrificTeamUp();
        Permanent firstSource = addPermanent(player1Id, createCreature("First source", 2, 2));
        Permanent secondSource = addPermanent(player1Id, createCreature("Second source", 3, 3));
        Permanent victim = addPermanent(player2Id, createCreature("Victim", 7, 7));
        StackEntry entry = createMultiTargetEntry(card, player1Id,
                List.of(victim.getId(), firstSource.getId(), secondSource.getId()));

        stubDamagePreventable();
        stubNoDamageMultiplier();
        stubCreatureDamageCore(victim, 7);
        stubCreatureSourceRedirects();
        when(gameQueryService.findPermanentById(gd, firstSource.getId())).thenReturn(firstSource);
        when(gameQueryService.findPermanentById(gd, secondSource.getId())).thenReturn(secondSource);
        when(gameQueryService.findPermanentById(gd, victim.getId())).thenReturn(victim);
        when(gameQueryService.isCreature(gd, firstSource)).thenReturn(true);
        when(gameQueryService.isCreature(gd, secondSource)).thenReturn(true);
        when(gameQueryService.getPowerBasedDamage(gd, firstSource)).thenReturn(2);
        when(gameQueryService.getPowerBasedDamage(gd, secondSource)).thenReturn(3);
        when(gameQueryService.isPreventedFromDealingDamage(gd, firstSource)).thenReturn(false);
        when(gameQueryService.isPreventedFromDealingDamage(gd, secondSource)).thenReturn(false);
        when(gameQueryService.hasProtectionFromSource(eq(gd), eq(victim), any(Permanent.class))).thenReturn(false);
        stubNoKeywordsOnSourceWithDamageSource(entry, firstSource);
        stubNoKeywordsOnSourceWithDamageSource(entry, secondSource);

        handler.resolve(gd, entry, new TargetCreaturesDealPowerDamageToTargetEffect(1, 0));

        assertThat(victim.getMarkedDamage()).isEqualTo(5);
    }
}
