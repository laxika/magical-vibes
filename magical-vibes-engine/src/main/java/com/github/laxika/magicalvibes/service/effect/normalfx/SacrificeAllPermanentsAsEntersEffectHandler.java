package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.SacrificeAllPermanentsAsEntersEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Applies {@link SacrificeAllPermanentsAsEntersEffect} as its permanent enters: its controller sacrifices every
 * permanent they control that matches the filter, judged with the entering permanent as the source ("each other
 * permanent named Sheltered Valley"), all at once. The entering permanent and the permanents entering with it are not
 * on the battlefield yet, so none of them is sacrificed. The caller skips it when the entering permanent will have lost
 * the ability.
 */
@Component
@RequiredArgsConstructor
public class SacrificeAllPermanentsAsEntersEffectHandler {

    private final DestructionSupport destructionSupport;
    private final PredicateEvaluationService predicateEvaluationService;

    public void applyIfPresent(GameData gameData, UUID controllerId, Permanent enteringPermanent) {
        SacrificeAllPermanentsAsEntersEffect effect = enteringPermanent.getCard()
                .getStaticEffects().stream()
                .filter(SacrificeAllPermanentsAsEntersEffect.class::isInstance)
                .map(SacrificeAllPermanentsAsEntersEffect.class::cast)
                .findFirst()
                .orElse(null);
        if (effect == null) {
            return;
        }
        FilterContext context = FilterContext.of(gameData)
                .withSourceCardId(enteringPermanent.getCard().getId())
                .withSourceControllerId(controllerId)
                .withSourcePermanentSnapshot(enteringPermanent);
        List<UUID> matching = gameData.playerBattlefields.getOrDefault(controllerId, List.of()).stream()
                .filter(permanent -> !permanent.getId().equals(enteringPermanent.getId()))
                .filter(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                        permanent, effect.filter(), context))
                .map(Permanent::getId)
                .toList();
        if (!matching.isEmpty()) {
            destructionSupport.performSimultaneousSacrifice(gameData, matching);
        }
    }
}
