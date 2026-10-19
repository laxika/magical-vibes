package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeTargetPermanentAtEndStepAndGainLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.action.DelayedSacrificeTargetPermanentAtEndStep;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class SacrificeTargetPermanentAtEndStepAndGainLifeEqualToToughnessEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final TriggerCollectionService triggerCollectionService;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeTargetPermanentAtEndStepAndGainLifeEqualToToughnessEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY) {
            sacrificeAndGainLife(gameData, entry);
            return;
        }
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (!entry.getControllerId().equals(targetControllerId)) {
            return;
        }

        gameData.queueDelayedAction(new DelayedSacrificeTargetPermanentAtEndStep(
                target.getId(), entry.getControllerId(), entry.getCard()));
        gameLogService.append(gameData,
                GameLog.cardThen(target.getCard(), " will be sacrificed at the beginning of the next end step."));
        log.info("Game {} - {} scheduled for sacrifice and life gain at end step",
                gameData.id, target.getCard().getName());
    }

    /**
     * Resolves the delayed "at the beginning of the next end step, sacrifice it. If you do, you gain
     * life equal to its toughness" trigger once it has gone on the stack, so players can respond to it.
     */
    private void sacrificeAndGainLife(GameData gameData, StackEntry entry) {
        Permanent permanent = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (permanent == null
                || !entry.getControllerId().equals(
                        gameQueryService.findPermanentController(gameData, permanent.getId()))) {
            return;
        }

        int toughness = Math.max(0, gameQueryService.getEffectiveToughness(gameData, permanent));
        if (!permanentRemovalService.sacrificePermanentToGraveyard(gameData, permanent)) {
            return;
        }
        triggerCollectionService.checkAllyPermanentSacrificedTriggers(
                gameData, entry.getControllerId(), permanent.getCard());
        gameLogService.append(gameData, GameLog.isSacrificed(permanent.getCard()));
        lifeSupport.applyGainLife(gameData, entry.getControllerId(), toughness,
                entry.getCard().getName(), entry.getCard(), StackEntryType.TRIGGERED_ABILITY,
                entry.getControllerId());
        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
