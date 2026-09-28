package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnChosenPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a non-targeting counter placement choice across all battlefields. */
@Component
@RequiredArgsConstructor
public class PutCounterOnChosenPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCounterOnChosenPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutCounterOnChosenPermanentEffect) effect;
        int count = amountEvaluationService.evaluate(gameData, e.amount(),
                AmountContext.forStackEntry(entry, null));
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                .withSourceControllerId(entry.getControllerId())
                .withXValue(entry.getXValue());

        List<UUID> eligibleIds = new ArrayList<>();
        gameData.forEachPermanent((ownerId, permanent) -> {
            if (e.predicate() == null
                    || predicateEvaluationService.matchesPermanentPredicate(permanent, e.predicate(), filterContext)) {
                eligibleIds.add(permanent.getId());
            }
        });

        if (eligibleIds.isEmpty()) {
            if (entry.getCard() != null) {
                gameLogService.append(gameData,
                        GameLog.cardThen(entry.getCard(), ": no eligible permanent to put counters on."));
            }
            return;
        }

        if (eligibleIds.size() == 1) {
            placeCounter(gameData, eligibleIds.getFirst(), entry, e, count);
            return;
        }

        playerInputService.beginMultiPermanentChoice(gameData, entry.getControllerId(), eligibleIds, 1,
                new MultiPermanentChoiceContext.AnyPermanentCounterPlacement(e.counterType(), count),
                "Choose a permanent to put counters on.");
    }

    private void placeCounter(GameData gameData, UUID permanentId, StackEntry entry,
                              PutCounterOnChosenPermanentEffect effect, int count) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (target != null) {
            permanentCounterSupport.placeCounterOnPermanent(gameData, entry, target,
                    effect.counterType(), count);
        }
    }
}
