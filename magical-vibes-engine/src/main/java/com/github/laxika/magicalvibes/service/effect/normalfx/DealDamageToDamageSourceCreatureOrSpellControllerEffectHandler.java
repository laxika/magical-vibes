package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToDamageSourceCreatureOrSpellControllerEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves Tephraderm's damage reflection trigger. */
@Component
@RequiredArgsConstructor
public class DealDamageToDamageSourceCreatureOrSpellControllerEffectHandler
        implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToDamageSourceCreatureOrSpellControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var damageEffect = (DealDamageToDamageSourceCreatureOrSpellControllerEffect) effect;
        if (damageEffect.amount() <= 0) return;

        UUID sourceCreatureId = damageEffect.damageSourceCreatureId();
        if (sourceCreatureId != null) {
            Permanent sourceCreature = gameQueryService.findPermanentById(gameData, sourceCreatureId);
            if (sourceCreature == null || !gameQueryService.isCreature(gameData, sourceCreature)) return;

            int damage = gameQueryService.applyDamageMultiplier(gameData, damageEffect.amount(), entry);
            damageSupport.dealCreatureDamage(gameData, entry, sourceCreature, damage);
            gameOutcomeService.checkWinCondition(gameData);
            return;
        }

        UUID spellControllerId = damageEffect.spellControllerId();
        if (spellControllerId == null || !gameData.playerIds.contains(spellControllerId)
                || damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) {
            return;
        }

        int damage = gameQueryService.applyDamageMultiplier(gameData, damageEffect.amount(), entry);
        damageSupport.dealDamageToPlayer(gameData, entry, spellControllerId, damage);
        gameOutcomeService.checkWinCondition(gameData);
    }
}
