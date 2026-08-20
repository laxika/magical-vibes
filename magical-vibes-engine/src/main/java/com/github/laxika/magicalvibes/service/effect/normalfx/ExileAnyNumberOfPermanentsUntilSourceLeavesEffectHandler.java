package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAnyNumberOfPermanentsUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a resolution-time any-number permanent exile with source-leave returns. */
@Component
@RequiredArgsConstructor
public class ExileAnyNumberOfPermanentsUntilSourceLeavesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileAnyNumberOfPermanentsUntilSourceLeavesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exile = (ExileAnyNumberOfPermanentsUntilSourceLeavesEffect) effect;
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentSnapshot(entry.getSourcePermanentSnapshot())
                .withSourcePermanentId(entry.getSourcePermanentId());

        List<UUID> eligibleIds = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent permanent : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(permanent, exile.filter(), filterContext)) {
                    eligibleIds.add(permanent.getId());
                }
            }
        });

        if (!eligibleIds.isEmpty()) {
            playerInputService.beginMultiPermanentChoice(gameData, entry.getControllerId(), eligibleIds,
                    eligibleIds.size(),
                    new MultiPermanentChoiceContext.ExileAnyNumberUntilSourceLeaves(entry.getSourcePermanentId()),
                    "Exile any number of matching permanents.");
        }
    }
}
