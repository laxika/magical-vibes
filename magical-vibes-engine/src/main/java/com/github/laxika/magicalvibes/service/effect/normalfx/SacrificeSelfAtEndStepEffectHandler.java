package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link SacrificeSelfAtEndStepEffect} by scheduling the source permanent, or the
 * attached permanent for its attached form, for sacrifice at the beginning of the next end step.
 * The delayed ability is put on the stack by {@code StepTriggerService.handleEndStepTriggers}.
 * Used by Brackwater Elemental's attack/block trigger.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SacrificeSelfAtEndStepEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeSelfAtEndStepEffect.class;
    }

    /**
     * The delayed triggered ability "at the beginning of the next end step, sacrifice it" of a spell or ability of
     * {@code sourceCard} (with the source permanent {@code makerSourcePermanentId}, or none) controlled by
     * {@code controllerId}, about the permanent {@code permanentId}: its own source for the plain form, which then
     * sacrifices itself, or the permanent the source is attached to.
     */
    public static TemporaryGlobalTriggeredAbility sacrificeAtNextEndStep(SacrificeSelfAtEndStepEffect sacrifice,
                                                                         UUID permanentId, UUID controllerId,
                                                                         Card sourceCard,
                                                                         UUID makerSourcePermanentId) {
        if (sacrifice.attachedPermanent()) {
            return TemporaryGlobalTriggeredAbility.atNextStep(Triggers.endStep(ActivePlayer.ANY), controllerId,
                    sourceCard, makerSourcePermanentId, permanentId, SacrificeSelfEffect.ofTriggeringPermanent());
        }
        return TemporaryGlobalTriggeredAbility.atNextStep(Triggers.endStep(ActivePlayer.ANY), controllerId, sourceCard,
                permanentId, null, new SacrificeSelfEffect(sacrifice.currentControllerSacrifices()));
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var sacrifice = (SacrificeSelfAtEndStepEffect) effect;
        UUID sourceId = sacrifice.attachedPermanent()
                ? entry.getTargetId()
                : entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetId();
        if (sourceId == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, sourceId);
        if (source == null) {
            return;
        }

        gameData.temporaryGlobalTriggeredAbilities.add(sacrificeAtNextEndStep(
                sacrifice, sourceId, entry.getControllerId(), entry.getCard(), entry.getSourcePermanentId()));

        String logEntry = source.getCard().getName() + " will be sacrificed at the beginning of the next end step.";
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} scheduled for sacrifice at end step", gameData.id, source.getCard().getName());
    }
}
