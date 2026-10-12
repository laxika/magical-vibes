package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesExceptThoseSharingConvokeCreatureTypeEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Resolves Everything Comes to Dust's single simultaneous exile of its creatures, artifacts, and enchantments. */
@Component
@RequiredArgsConstructor
public class ExileCreaturesExceptThoseSharingConvokeCreatureTypeEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileCreaturesExceptThoseSharingConvokeCreatureTypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Permanent> convokingCreatures = entry.getConvokeCreatureIds().stream()
                .map(id -> gameQueryService.findPermanentById(gameData, id))
                .filter(java.util.Objects::nonNull)
                .toList();
        // A convoker that already left the battlefield is judged by the creature types it had when
        // it was recorded as convoking (last-known information).
        List<Card> lastKnownConvokers = entry.getConvokeCreatureIds().stream()
                .filter(id -> gameQueryService.findPermanentById(gameData, id) == null)
                .map(entry::lastKnownPermanentCard)
                .filter(java.util.Objects::nonNull)
                .toList();
        var exileEffect = (ExileCreaturesExceptThoseSharingConvokeCreatureTypeEffect) effect;
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId());
        List<Permanent> toExile = new ArrayList<>();

        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent permanent : battlefield) {
                boolean exiledAnyway = exileEffect.alsoExile() != null
                        && predicateEvaluationService.matchesPermanentPredicate(
                        permanent, exileEffect.alsoExile(), filterContext);
                if (exiledAnyway
                        || (gameQueryService.isCreature(gameData, permanent)
                        && convokingCreatures.stream().noneMatch(convoker ->
                        gameQueryService.shareCreatureType(gameData, permanent, convoker))
                        && lastKnownConvokers.stream().noneMatch(card ->
                        gameQueryService.shareCreatureType(gameData, permanent, card)))) {
                    toExile.add(permanent);
                }
            }
        });

        permanentRemovalService.beginPermanentLeaveBatch(gameData);
        try {
            permanentRemovalService.performSimultaneousRemovals(gameData, toExile, () -> {
                for (Permanent permanent : toExile) {
                    if (permanentRemovalService.removePermanentToExile(gameData, permanent)) {
                        gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), " is exiled."));
                    }
                }
            });
        } finally {
            permanentRemovalService.endPermanentLeaveBatch(gameData);
        }

        entry.setEventValue(toExile.size());
        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
