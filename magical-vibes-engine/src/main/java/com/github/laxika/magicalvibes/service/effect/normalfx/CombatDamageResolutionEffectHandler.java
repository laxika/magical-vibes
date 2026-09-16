package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CombatDamageResolutionEffect;
import com.github.laxika.magicalvibes.service.combat.CombatDamageService;
import org.springframework.stereotype.Component;

/** Resolves the internal stack object created for Stack of Paperwork's combat rule. */
@Component
public class CombatDamageResolutionEffectHandler implements NormalEffectHandlerBean {

    private final CombatDamageService combatDamageService;

    public CombatDamageResolutionEffectHandler(CombatDamageService combatDamageService) {
        this.combatDamageService = combatDamageService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CombatDamageResolutionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        combatDamageService.resolvePendingCombatDamage(gameData);
    }
}
