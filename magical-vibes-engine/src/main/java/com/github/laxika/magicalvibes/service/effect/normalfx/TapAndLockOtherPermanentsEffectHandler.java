package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TapAndLockOtherPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TapAndLockOtherPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final TapUntapSupport tapUntapSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TapAndLockOtherPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        TapAndLockOtherPermanentsEffect tapAndLock = (TapAndLockOtherPermanentsEffect) effect;
        UUID sourcePermanentId = entry.getSourcePermanentId();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());

        int[] newlyTappedCount = {0};
        int[] lockedCount = {0};
        gameData.forEachPermanent((playerId, permanent) -> {
            if (sourcePermanentId != null && sourcePermanentId.equals(permanent.getId())) {
                return;
            }
            if (!predicateEvaluationService.matchesPermanentPredicate(permanent, tapAndLock.filter(), filterContext)) {
                return;
            }

            if (tapUntapSupport.tapPermanent(gameData, permanent)) {
                newlyTappedCount[0]++;
            }
            if (sourcePermanentId != null) {
                permanent.getUntapPreventedByPermanentIds().add(sourcePermanentId);
                lockedCount[0]++;
            }
        });

        gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                .text(" taps and locks " + lockedCount[0] + " permanent(s) while it remains tapped.")
                .build());
        log.info("Game {} - {} tapped {} newly and locked {} other permanent(s) while source remains tapped",
                gameData.id, entry.getCard().getName(), newlyTappedCount[0], lockedCount[0]);
    }
}
