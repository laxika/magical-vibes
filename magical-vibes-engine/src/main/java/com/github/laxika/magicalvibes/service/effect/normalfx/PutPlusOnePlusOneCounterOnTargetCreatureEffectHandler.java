package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutPlusOnePlusOneCounterOnTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutPlusOnePlusOneCounterOnTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutPlusOnePlusOneCounterOnTargetCreatureEffect) effect;
        // Multi-group spell (e.g. River Heralds' Boon): each effect uses its own target
        // set by EffectResolutionService via the SpellTarget index mapping.
        // If targetId is null, the optional target wasn't chosen — do nothing.
        // Only applies to spell entries — triggered/activated abilities use their own target layout.
        if (entry.getEntryType() != StackEntryType.TRIGGERED_ABILITY
                && entry.getEntryType() != StackEntryType.ACTIVATED_ABILITY
                && entry.getCard().getSpellTargets().size() > 1) {
            if (entry.getTargetId() != null) {
                Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
                if (target == null) return;
                if (gameQueryService.cantHaveCounters(gameData, target)) return;
                permanentCounterSupport.applyPlusOnePlusOneCounters(gameData, entry, target, e.count());
            }
            return;
        }

        // Single-group multi-target: apply counters to each valid target (e.g. Dual Shot pattern)
        if (entry.getTargetIds() != null && !entry.getTargetIds().isEmpty()) {
            for (UUID targetId : entry.getTargetIds()) {
                Permanent target = gameQueryService.findPermanentById(gameData, targetId);
                if (target == null) {
                    continue; // Partially resolves — skip removed targets
                }
                if (gameQueryService.cantHaveCounters(gameData, target)) {
                    continue;
                }
                permanentCounterSupport.applyPlusOnePlusOneCounters(gameData, entry, target, e.count());
            }
            return;
        }

        // Single-target fallback
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            log.info("Game {} - Target creature no longer on battlefield, effect fizzles", gameData.id);
            return;
        }

        if (gameQueryService.cantHaveCounters(gameData, target)) {
            return;
        }

        permanentCounterSupport.applyPlusOnePlusOneCounters(gameData, entry, target, e.count());
    }
}
