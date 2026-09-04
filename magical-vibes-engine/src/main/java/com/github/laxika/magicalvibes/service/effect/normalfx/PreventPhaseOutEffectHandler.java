package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutSubject;
import com.github.laxika.magicalvibes.model.effect.PreventPhaseOutEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link PreventPhaseOutEffect}: marks the subject permanent as unable to phase out until
 * the resolving controller's next upkeep. If the subject already left the battlefield the ability
 * simply does nothing.
 */
@Component
@RequiredArgsConstructor
public class PreventPhaseOutEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventPhaseOutEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PreventPhaseOutEffect e = (PreventPhaseOutEffect) effect;
        UUID subjectId = e.subject() == PhaseOutSubject.SOURCE
                ? entry.getSourcePermanentId()
                : entry.getTargetId();
        if (subjectId == null) {
            return;
        }
        Permanent subject = gameQueryService.findPermanentById(gameData, subjectId);
        if (subject == null) {
            return;
        }

        subject.preventPhaseOutUntilUpkeepOf(entry.getControllerId());
        gameLogService.append(gameData, GameLog.cardThen(subject.getCard(), " can't phase out."));
    }
}
