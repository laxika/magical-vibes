package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureWhileTargetTappedEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves Zygon Infiltrator's copy effect and keys its duration to the tapped target. */
@Slf4j
@Component
@RequiredArgsConstructor
public class BecomeCopyOfTargetCreatureWhileTargetTappedEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentCopierService permanentCopierService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeCopyOfTargetCreatureWhileTargetTappedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        UUID sourceId = entry.getSourcePermanentId();
        if (targetId == null || sourceId == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        Permanent source = gameQueryService.findPermanentById(gameData, sourceId);
        if (target == null || source == null || !target.isTapped()) {
            return;
        }

        if (!source.isCopyUntilEndOfTurn()) {
            source.setPreCopyCard(source.getCard());
        }
        String originalName = source.getCard().getName();
        permanentCopierService.applyCloneCopy(source, target, null, null);
        source.setCopyUntilEndOfTurn(true);

        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), targetId, entry.getControllerId(), effect,
                sourceId, null, null, EffectDuration.WHILE_SOURCE_REMAINS_TAPPED, 0));

        gameLogService.append(gameData, GameLog.text(
                originalName + " becomes a copy of " + target.getCard().getName()
                        + " for as long as it remains tapped."));
        log.info("Game {} - {} becomes a copy of {} while the target remains tapped",
                gameData.id, originalName, target.getCard().getName());
    }
}
