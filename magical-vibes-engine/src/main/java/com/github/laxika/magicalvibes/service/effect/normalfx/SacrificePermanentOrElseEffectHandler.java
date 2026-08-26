package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentOrElseEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves a mandatory active-player sacrifice with a fallback when no legal sacrifice exists. */
@Component
@RequiredArgsConstructor
public class SacrificePermanentOrElseEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificePermanentOrElseEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SacrificePermanentOrElseEffect) effect;
        UUID activePlayerId = entry.getTargetId();
        if (activePlayerId == null || !gameData.playerIds.contains(activePlayerId)) {
            return;
        }

        if (!gameQueryService.canEffectCauseSacrifice(gameData, activePlayerId, entry.getControllerId())) {
            insertElseEffect(entry, effect, e.elseEffect());
            return;
        }

        List<UUID> eligibleIds = eligiblePermanentIds(gameData, entry, e, activePlayerId);
        if (eligibleIds.isEmpty()) {
            insertElseEffect(entry, effect, e.elseEffect());
        } else if (eligibleIds.size() == 1) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, eligibleIds.getFirst());
            if (permanent != null) {
                destructionSupport.sacrificeAndLog(gameData, permanent, activePlayerId);
            }
        } else {
            playerInputService.beginMultiPermanentChoice(
                    gameData,
                    activePlayerId,
                    eligibleIds,
                    1,
                    new MultiPermanentChoiceContext.ForcedSacrifice(
                            activePlayerId, List.of(), List.of()),
                    entry.getCard().getName() + " — Choose " + e.permanentDescription() + " to sacrifice.");
        }
    }

    private List<UUID> eligiblePermanentIds(GameData gameData, StackEntry entry,
                                             SacrificePermanentOrElseEffect effect,
                                             UUID activePlayerId) {
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentId(entry.getSourcePermanentId());
        List<UUID> eligibleIds = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(activePlayerId);
        if (battlefield == null) {
            return eligibleIds;
        }
        for (Permanent permanent : battlefield) {
            if (predicateEvaluationService.matchesPermanentPredicate(
                    permanent, effect.filter(), filterContext)
                    && !gameQueryService.cantBeSacrificed(gameData, permanent)) {
                eligibleIds.add(permanent.getId());
            }
        }
        return eligibleIds;
    }

    private void insertElseEffect(StackEntry entry, CardEffect currentEffect, CardEffect elseEffect) {
        int effectIndex = entry.getEffectsToResolve().indexOf(currentEffect);
        if (effectIndex < 0) {
            throw new IllegalStateException("SacrificePermanentOrElseEffect is not in its stack entry");
        }
        entry.insertEffectsToResolve(effectIndex + 1, List.of(elseEffect));
    }
}
