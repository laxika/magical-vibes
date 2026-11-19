package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutOrRemoveCounterIfAttackedOrBlockedSinceLastUpkeepEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


/**
 * Resolves the upkeep counter adjustment for permanents that attacked or blocked during the
 * window since the previous relevant upkeep.
 */
@Component
@RequiredArgsConstructor
public class PutOrRemoveCounterIfAttackedOrBlockedSinceLastUpkeepEffectHandler
        implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutOrRemoveCounterIfAttackedOrBlockedSinceLastUpkeepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutOrRemoveCounterIfAttackedOrBlockedSinceLastUpkeepEffect) effect;
        Permanent permanent = subjectResolutionService.permanent(gameData, entry, effect, Subjects.of(e.reference()));
        if (permanent == null) {
            return;
        }

        boolean attackedOrBlocked = permanent.attackedOrBlockedSinceUpkeepOf(entry.getControllerId());

        String counterName = permanentCounterSupport.counterTypeName(e.counterType());
        if (attackedOrBlocked) {
            permanentCounterSupport.placeCounterOnPermanent(gameData, entry, permanent, e.counterType(), 1);
            return;
        }

        int current = permanent.getCounterCount(e.counterType());
        if (current <= 0) {
            return;
        }
        permanent.setCounterCount(e.counterType(), current - 1);
        gameLogService.append(gameData, GameLog.builder().card(permanent.getCard())
                .text(" removes a " + counterName + " counter.").build());
    }
}
