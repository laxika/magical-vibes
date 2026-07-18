package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCanBeBlockedOnlyByFilterToOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrantCanBeBlockedOnlyByFilterToOwnCreaturesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantCanBeBlockedOnlyByFilterToOwnCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (GrantCanBeBlockedOnlyByFilterToOwnCreaturesEffect) effect;
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) {
            return;
        }

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                .withSourceControllerId(entry.getControllerId());
        CanBeBlockedOnlyByFilterEffect restriction =
                new CanBeBlockedOnlyByFilterEffect(grant.blockerPredicate(), grant.allowedBlockersDescription());

        int count = 0;
        for (Permanent permanent : battlefield) {
            if (!gameQueryService.isCreature(gameData, permanent)) {
                continue;
            }
            if (grant.creatureFilter() != null
                    && !predicateEvaluationService.matchesPermanentPredicate(permanent, grant.creatureFilter(), filterContext)) {
                continue;
            }
            permanent.getBlockRestrictionsUntilEndOfTurn().add(restriction);
            count++;
        }

        String logEntry = entry.getCard().getName() + " makes " + count + " creature(s) unblockable except by "
                + grant.allowedBlockersDescription() + " this turn.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().card(entry.getCard()).text(" makes " + count + " creature(s) unblockable except by " + grant.allowedBlockersDescription() + " this turn.").build());
        log.info("Game {} - {} restricts blockers of {} own creature(s) to {}",
                gameData.id, entry.getCard().getName(), count, grant.allowedBlockersDescription());
    }
}
