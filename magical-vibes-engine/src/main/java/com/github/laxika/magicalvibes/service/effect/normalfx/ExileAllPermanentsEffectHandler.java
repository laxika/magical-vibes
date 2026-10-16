package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DimensionalBreachUpkeepReturn;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileAllPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final ExileSupport exileSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileAllPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileAllPermanentsEffect) effect;
        List<Permanent> toExile = new ArrayList<>();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentId(entry.getSourcePermanentId())
                .withXValue(entry.getXValue());

        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent perm : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(perm, e.filter(), filterContext)) {
                    toExile.add(perm);
                }
            }
        });

        UUID sourcePermanentId = e.trackWithSource()
                ? entry.getSourcePermanentId() != null
                        ? entry.getSourcePermanentId()
                        : findSourcePermanentId(gameData, entry)
                : null;

        permanentRemovalService.beginPermanentLeaveBatch(gameData);
        try {
            permanentRemovalService.performSimultaneousRemovals(gameData, toExile, () -> {
            for (Permanent perm : toExile) {
                List<Card> leavingCards = new ArrayList<>(perm.cardsLeavingBattlefield());
                boolean removed;
                if (sourcePermanentId != null) {
                    removed = permanentRemovalService.removePermanentToExile(gameData, perm, sourcePermanentId);
                } else {
                    removed = permanentRemovalService.removePermanentToExile(gameData, perm);
                }
                for (Card exiledCard : leavingCards) {
                    UUID exiledCardId = exiledCard.getId();
                    if (e.perpetualCastCostIncrease() != 0) {
                        gameData.perpetualGenericCastCostIncreases.merge(
                                exiledCardId, e.perpetualCastCostIncrease(), Integer::sum);
                    }
                    if (e.perpetualEnterTapped()) {
                        gameData.perpetualEnterTappedCardIds.add(exiledCardId);
                    }
                    if (e.ownerMayPlayWhileExiled()) {
                        UUID ownerId = gameQueryService.findExileOwnerById(gameData, exiledCardId);
                        if (ownerId != null) {
                            exileSupport.grantPlayWhileExiled(gameData, exiledCardId, ownerId);
                        }
                    }
                    if (e.controllerMayPlayWhileExiled()
                            && removed
                            && gameData.findExiledCard(exiledCardId) != null) {
                        exileSupport.grantPlayWhileExiled(gameData, exiledCardId, entry.getControllerId());
                        if (e.controllerMaySpendAnyManaType()) {
                            gameData.exilePlayAnyManaTypeWhileExiled.add(exiledCardId);
                        }
                    }
                }
                gameLogService.append(gameData, GameLog.cardThen(perm.getCard(), " is exiled."));
                log.info("Game {} - {} is exiled by {}",
                        gameData.id, perm.getCard().getName(), entry.getCard().getName());
            }
            });
        } finally {
            permanentRemovalService.endPermanentLeaveBatch(gameData);
        }

        entry.setEventValue(toExile.size());

        if (e.returnOneAtEachUpkeep() && e.trackWithSource() && !toExile.isEmpty()) {
            gameData.queueDelayedAction(new DimensionalBreachUpkeepReturn(entry.getCard(), entry.getControllerId()));
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    private UUID findSourcePermanentId(GameData gameData, StackEntry entry) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) {
            return entry.getCard().getId();
        }
        for (Permanent permanent : battlefield) {
            if (permanent.getCard() == entry.getCard()) {
                return permanent.getId();
            }
        }
        return entry.getCard().getId();
    }
}
