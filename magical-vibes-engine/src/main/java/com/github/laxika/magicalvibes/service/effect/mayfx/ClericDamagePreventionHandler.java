package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToControllerPerClericEffect;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.combat.CombatDamageService;
import com.github.laxika.magicalvibes.service.effect.normalfx.DamageSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/** Completes one optional Alchemist prevention and continues the same damage event. */
@Component
@RequiredArgsConstructor
public class ClericDamagePreventionHandler implements MayEffectHandlerBean {
    private final DamagePreventionService damagePreventionService;
    private final ObjectProvider<DamageSupport> damageSupport;
    private final ObjectProvider<CombatDamageService> combatDamageService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventDamageToControllerPerClericEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var effect = (PreventDamageToControllerPerClericEffect) ability.effects().getFirst();
        int amount = ability.eventValue();
        if (accepted) amount = Math.max(0, amount
                - damagePreventionService.clericPreventionAmount(gameData, ability.controllerId()));
        if (effect.combatDamage()) {
            combatDamageService.getObject().completeClericPreventionChoice(gameData, ability, amount);
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }
        StackEntry entry = new StackEntry(effect.damageEntryType(), ability.sourceCard(),
                ability.sourceControllerId(), "Damage", List.of(), effect.damagedPlayerId(),
                ability.sourcePermanentId());
        entry.setSourcePermanentSnapshot(ability.sourcePermanentSnapshot());
        boolean previous = gameData.unpreventableDamageInProgress;
        gameData.unpreventableDamageInProgress = previous || effect.unpreventableDamage();
        try {
            if (!damagePreventionService.queueClericPreventionChoice(gameData, entry,
                    effect.damagedPlayerId(), amount, false, effect.remainingAlchemistIds())) {
                damageSupport.getObject().finishPlayerDamageAfterClericChoice(
                        gameData, entry, effect.damagedPlayerId(), amount);
            }
        } finally {
            gameData.unpreventableDamageInProgress = previous;
        }
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
