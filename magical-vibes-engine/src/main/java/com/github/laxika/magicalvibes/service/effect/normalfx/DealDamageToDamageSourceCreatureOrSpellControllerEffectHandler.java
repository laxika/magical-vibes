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

/**
 * Resolves Tephraderm's damage reflection trigger: the damage dealt (the entry's event value) to the triggering
 * permanent if it is still a creature on the battlefield, or to the recorded controller of a source that was never a
 * permanent (a spell).
 */
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
        int damage = entry.getEventValue();
        if (damage <= 0) return;

        UUID sourceCreatureId = entry.getTriggeringPermanentId();
        if (sourceCreatureId != null) {
            Permanent sourceCreature = gameQueryService.findPermanentById(gameData, sourceCreatureId);
            if (sourceCreature == null || !gameQueryService.isCreature(gameData, sourceCreature)) return;

            damageSupport.dealCreatureDamage(gameData, entry, sourceCreature, damage);
            gameOutcomeService.checkWinCondition(gameData);
            return;
        }

        UUID spellControllerId = entry.getTriggeringPermanentControllerId();
        if (spellControllerId == null || !gameData.playerIds.contains(spellControllerId)
                || damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) {
            return;
        }

        damageSupport.dealDamageToPlayer(gameData, entry, spellControllerId, damage);
        gameOutcomeService.checkWinCondition(gameData);
    }
}
