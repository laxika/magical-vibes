package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachControlledPermanentBecomesCopyOfTargetNonAuraPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class EachControlledPermanentBecomesCopyOfTargetNonAuraPermanentEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PermanentCopierService permanentCopierService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachControlledPermanentBecomesCopyOfTargetNonAuraPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var copyEffect = (EachControlledPermanentBecomesCopyOfTargetNonAuraPermanentEffect) effect;
        UUID targetId = entry.getTargetId();
        if (targetId == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            log.info("Game {} - Mirrorform target no longer exists", gameData.id);
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) {
            return;
        }

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() == null ? null : entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentSnapshot(entry.getSourcePermanentSnapshot())
                .withXValue(entry.getXValue());
        List<Permanent> permanents = new ArrayList<>();
        for (Permanent permanent : List.copyOf(battlefield)) {
            if (predicateEvaluationService.matchesPermanentPredicate(
                    permanent, copyEffect.filter(), filterContext)) {
                permanents.add(permanent);
            }
        }

        for (Permanent permanent : permanents) {
            permanentCopierService.applyCloneCopy(permanent, target, null, null);
        }

        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" makes " + permanents.size() + " permanent(s) a copy of "
                        + target.getCard().getName() + ".")
                .build());
        log.info("Game {} - Mirrorform copies {} onto {} permanents", gameData.id,
                target.getCard().getName(), permanents.size());
    }
}
