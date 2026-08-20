package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnChosenOwnPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PutCounterOnChosenOwnPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCounterOnChosenOwnPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutCounterOnChosenOwnPermanentEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                .withSourceControllerId(controllerId)
                .withXValue(entry.getXValue());
        List<UUID> eligibleIds = new ArrayList<>();
        for (Permanent permanent : battlefield) {
            if (predicateEvaluationService.matchesPermanentPredicate(permanent, e.predicate(), filterContext)) {
                eligibleIds.add(permanent.getId());
            }
        }

        if (eligibleIds.isEmpty()) {
            if (entry.getCard() != null) {
                gameLogService.append(gameData,
                        GameLog.cardThen(entry.getCard(), ": no eligible permanent to put counters on."));
            }
            return;
        }

        if (eligibleIds.size() == 1) {
            Permanent chosen = gameQueryService.findPermanentById(gameData, eligibleIds.getFirst());
            if (chosen != null) {
                entry.setChosenPermanentId(chosen.getId());
                int placed = permanentCounterSupport.placeCounterOnPermanent(gameData, entry, chosen,
                        e.counterType(), e.count());
                if (e.recordPlacement() && placed > 0
                        && !entry.getCounteredPermanentIdsThisResolution().contains(chosen.getId())) {
                    entry.getCounteredPermanentIdsThisResolution().add(chosen.getId());
                    entry.setEventValue(entry.getEventValue() + 1);
                }
            }
            return;
        }

        playerInputService.beginMultiPermanentChoice(gameData, controllerId, eligibleIds, 1,
                new MultiPermanentChoiceContext.OwnPermanentCounterPlacementWithChosenReference(
                        e.counterType(), e.count(), e.recordPlacement()),
                "Choose a permanent to put counters on.");
    }
}
