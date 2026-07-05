package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TapTargetPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final TapUntapSupport tapUntapSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TapTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        // Mixed multi-target spell: this effect is bound to a single-target group that is
        // only one of several target groups (e.g. Vibrant Outburst: "3 damage to any target.
        // Tap up to one target creature."). EffectResolutionService has already set
        // entry.targetId to this group's chosen target (or null if the optional target was
        // omitted), so tap only that target rather than every target in the spell.
        if (entry.getCard() != null && isSingleTargetGroupInMultiGroupSpell(entry, effect)) {
            Permanent boundTarget = gameQueryService.findPermanentById(gameData, entry.getTargetId());
            if (boundTarget != null) {
                tapTarget(gameData, entry, boundTarget);
            }
            return;
        }

        // Multi-target: tap each valid target
        if (entry.getTargetIds() != null && !entry.getTargetIds().isEmpty()) {
            for (UUID targetId : entry.getTargetIds()) {
                Permanent target = gameQueryService.findPermanentById(gameData, targetId);
                if (target == null) {
                    continue;
                }
                tapUntapSupport.tapPermanent(gameData, target);
                String logMsg = entry.getCard().getName() + " taps " + target.getCard().getName() + ".";
                gameBroadcastService.logAndBroadcast(gameData, logMsg);
                log.info("Game {} - {} taps {}", gameData.id, entry.getCard().getName(), target.getCard().getName());
            }
            return;
        }

        // Single-target fallback
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        tapTarget(gameData, entry, target);
    }

    private void tapTarget(GameData gameData, StackEntry entry, Permanent target) {
        tapUntapSupport.tapPermanent(gameData, target);

        String logEntry = entry.getCard().getName() + " taps " + target.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} taps {}", gameData.id, entry.getCard().getName(), target.getCard().getName());
    }

    private boolean isSingleTargetGroupInMultiGroupSpell(StackEntry entry, CardEffect effect) {
        int targetIdx = entry.getCard().getEffectTargetIndex(effect);
        return targetIdx >= 0
                && entry.getCard().getSpellTargets().size() > 1
                && targetIdx < entry.getCard().getSpellTargets().size()
                && entry.getCard().getSpellTargets().get(targetIdx).getMaxTargets() == 1;
    }
}
