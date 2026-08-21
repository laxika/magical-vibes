package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
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
    private final PermanentRemovalService permanentRemovalService;
    private final GameLogService gameLogService;

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

    public void completeChoice(GameData gameData, List<UUID> permanentIds, UUID sourcePermanentId) {
        for (UUID permanentId : permanentIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent == null) {
                continue;
            }

            Card card = permanent.getOriginalCard();
            UUID controllerId = gameQueryService.findPermanentController(gameData, permanentId);
            UUID ownerId = gameData.stolenCreatures.getOrDefault(permanentId, controllerId);
            boolean token = card.isToken();

            if (!permanentRemovalService.removePermanentToExile(gameData, permanent)) {
                continue;
            }
            gameLogService.append(gameData, GameLog.cardThen(card, " is exiled."));
            if (sourcePermanentId != null && !token) {
                gameData.addExileReturnOnPermanentLeave(
                        sourcePermanentId, new PendingExileReturn(card, ownerId));
            }
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
