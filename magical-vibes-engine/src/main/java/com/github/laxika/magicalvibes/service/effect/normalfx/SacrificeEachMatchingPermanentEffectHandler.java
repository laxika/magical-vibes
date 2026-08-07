package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Sacrifices every permanent matching the effect's filter, each on behalf of its own controller.
 * Matching permanents are collected across all battlefields before the first sacrifice, so a
 * permanent leaving play mid-sweep cannot change which others are sacrificed.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SacrificeEachMatchingPermanentEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeEachMatchingPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SacrificeEachMatchingPermanentEffect) effect;
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId());

        List<Permanent> toSacrifice = new ArrayList<>();
        List<UUID> controllerIds = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent perm : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(perm, e.filter(), filterContext)) {
                    toSacrifice.add(perm);
                    controllerIds.add(playerId);
                }
            }
        });

        for (int i = 0; i < toSacrifice.size(); i++) {
            destructionSupport.sacrificeAndLog(gameData, toSacrifice.get(i), controllerIds.get(i));
        }
    }
}
