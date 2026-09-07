package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsOrElseEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves exact-count optional permanent sacrifices with an alternate effect on decline. */
@Component
@RequiredArgsConstructor
public class SacrificePermanentsOrElseEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificePermanentsOrElseEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SacrificePermanentsOrElseEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<UUID> eligibleIds = eligiblePermanentIds(gameData, entry, e);

        if (eligibleIds.size() < e.count()) {
            insertBranch(entry, effect, e.elseEffect());
            return;
        }

        playerInputService.beginMultiPermanentChoice(
                gameData,
                controllerId,
                eligibleIds,
                e.count(),
                new MultiPermanentChoiceContext.SacrificePermanentsOrElse(
                        e.count(), e.sacrificedEffect(), e.elseEffect()),
                entry.getCard().getName() + " — Choose " + e.count() + " "
                        + e.permanentDescription() + " to sacrifice (choose none to decline).");
    }

    private List<UUID> eligiblePermanentIds(GameData gameData, StackEntry entry,
                                             SacrificePermanentsOrElseEffect effect) {
        UUID controllerId = entry.getControllerId();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(controllerId)
                .withSourcePermanentId(entry.getSourcePermanentId());
        List<UUID> eligibleIds = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(
                        permanent, effect.filter(), filterContext)) {
                    eligibleIds.add(permanent.getId());
                }
            }
        }
        return eligibleIds;
    }

    private void insertBranch(StackEntry entry, CardEffect currentEffect, CardEffect branch) {
        int effectIndex = entry.getEffectsToResolve().indexOf(currentEffect);
        if (effectIndex < 0) {
            throw new IllegalStateException("SacrificePermanentsOrElseEffect is not in its stack entry");
        }
        entry.insertEffectsToResolve(effectIndex + 1, List.of(branch));
    }
}
