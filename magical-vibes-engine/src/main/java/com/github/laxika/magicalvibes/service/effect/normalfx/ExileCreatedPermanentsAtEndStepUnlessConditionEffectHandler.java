package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCreatedPermanentsAtEndStepUnlessConditionEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@Component
@RequiredArgsConstructor
public class ExileCreatedPermanentsAtEndStepUnlessConditionEffectHandler implements NormalEffectHandlerBean {

    private final ConditionEvaluationService conditionEvaluationService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileCreatedPermanentsAtEndStepUnlessConditionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exile = (ExileCreatedPermanentsAtEndStepUnlessConditionEffect) effect;
        if (exile.permanentIds() == null) {
            List<UUID> createdIds = List.copyOf(entry.getCreatedPermanentIds());
            if (!createdIds.isEmpty()) {
                gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(
                        entry.getControllerId(), entry.getCard(),
                        new TriggeredAbility(Triggers.endStep(ActivePlayer.ANY),
                                new ExileCreatedPermanentsAtEndStepUnlessConditionEffect(createdIds,
                                        exile.condition())),
                        entry.getSourcePermanentId(), null, null, DelayedReferents.NONE));
            }
            return;
        }

        if (exile.permanentIds().isEmpty()
                || conditionEvaluationService.isMet(gameData, exile.condition(), ConditionContext.forStackEntry(entry))) {
            return;
        }

        for (UUID permanentId : exile.permanentIds()) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent != null && permanentRemovalService.removePermanentToExile(gameData, permanent)) {
                gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), " is exiled."));
            }
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
