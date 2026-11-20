package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import com.github.laxika.magicalvibes.model.effect.TargetDealsPowerDamageToTargetEffect;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;

class TargetDealsPowerDamageToItselfEffectHandlerTest extends AbstractDamageHandlerTest {

    private TargetDealsPowerDamageToTargetEffectHandler handler;

    @Override
    protected void setUpHandler() {
        handler = new TargetDealsPowerDamageToTargetEffectHandler(damageSupport, gameQueryService, gameLogService,
                new SubjectResolutionService(gameQueryService, predicateEvaluationService,
                        org.mockito.Mockito.mock(com.github.laxika.magicalvibes.service.target.TargetLegalityService.class)));
    }

    @Test
    @DisplayName("Target creature deals its power as damage to itself — survives")
    void dealsPowerDamageToSelfAndSurvives() {
        Card spell = createCard("Wrack with Madness");
        spell.setColor(CardColor.RED);
        Permanent target = addPermanent(player2Id, createCreature("Wall of Swords", 3, 5));
        StackEntry entry = createEntry(spell, player1Id, target.getId());

        stubDamagePreventable();
        stubNoDamageMultiplier();
        stubCreatureDamageCore(target, 5);
        when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
        when(gameQueryService.getPowerBasedDamage(gd, target)).thenReturn(3);
        lenient().when(gameQueryService.isCreature(gd, target)).thenReturn(true);
        when(gameQueryService.isPreventedFromDealingDamage(gd, target)).thenReturn(false);
        when(gameQueryService.hasProtectionFromSource(eq(gd), eq(target), eq(target))).thenReturn(false);
        when(gameQueryService.findPermanentController(eq(gd), eq(target.getId()))).thenReturn(player2Id);
        stubNoKeywordsOnSourceWithDamageSource(entry, target);

        handler.resolve(gd, entry, TargetDealsPowerDamageToTargetEffect.toItself());

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        verify(permanentRemovalService, never()).removePermanentToGraveyard(any(), any());
        assertDamageToPermanent(target, 3, player2Id, target.getCard(), target.getId());
    }

    @Test
    @DisplayName("Marks lethal damage when its power is lethal to itself")
    void killsTargetWhenPowerIsLethal() {
        Card spell = createCard("Wrack with Madness");
        spell.setColor(CardColor.RED);
        Permanent target = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
        StackEntry entry = createEntry(spell, player1Id, target.getId());

        stubDamagePreventable();
        stubNoDamageMultiplier();
        stubCreatureDamageCore(target, 2);
        when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
        when(gameQueryService.getPowerBasedDamage(gd, target)).thenReturn(2);
        lenient().when(gameQueryService.isCreature(gd, target)).thenReturn(true);
        when(gameQueryService.isPreventedFromDealingDamage(gd, target)).thenReturn(false);
        when(gameQueryService.hasProtectionFromSource(eq(gd), eq(target), eq(target))).thenReturn(false);
        when(gameQueryService.findPermanentController(eq(gd), eq(target.getId()))).thenReturn(player2Id);
        stubNoKeywordsOnSourceWithDamageSource(entry, target);

        handler.resolve(gd, entry, TargetDealsPowerDamageToTargetEffect.toItself());

        // Lethal marked damage — the SBA check after resolution performs the destruction.
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertDamageToPermanent(target, 2, player2Id, target.getCard(), target.getId());
    }

    @Test
    @DisplayName("Multiplies the target creature's power before dealing self-damage")
    void appliesPowerMultiplier() {
        Card spell = createCard("Cut Propulsion");
        spell.setColor(CardColor.RED);
        Permanent target = addPermanent(player2Id, createCreature("Wall of Air", 1, 5));
        StackEntry entry = createEntry(spell, player1Id, target.getId());

        stubDamagePreventable();
        stubNoDamageMultiplier();
        stubCreatureDamageCore(target, 2);
        when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
        when(gameQueryService.getPowerBasedDamage(gd, target)).thenReturn(1);
        lenient().when(gameQueryService.isCreature(gd, target)).thenReturn(true);
        when(gameQueryService.isPreventedFromDealingDamage(gd, target)).thenReturn(false);
        when(gameQueryService.hasProtectionFromSource(eq(gd), eq(target), eq(target))).thenReturn(false);
        when(gameQueryService.findPermanentController(eq(gd), eq(target.getId()))).thenReturn(player2Id);
        stubNoKeywordsOnSourceWithDamageSource(entry, target);

        handler.resolve(gd, entry, TargetDealsPowerDamageToTargetEffect.toItself(2));

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertDamageToPermanent(target, 2, player2Id, target.getCard(), target.getId());
    }
}
