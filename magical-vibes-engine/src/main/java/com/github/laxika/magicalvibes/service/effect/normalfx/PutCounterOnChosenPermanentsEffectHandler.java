package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnChosenPermanentsEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves a controller's choice of up to N matching permanents for counter placement. */
@Component
@RequiredArgsConstructor
public class PutCounterOnChosenPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCounterOnChosenPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var typedEffect = (PutCounterOnChosenPermanentsEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<UUID> eligibleIds = new ArrayList<>();
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(controllerId, List.of())) {
            if (predicateEvaluationService.matchesPermanentPredicate(
                    gameData, permanent, typedEffect.permanentFilter())) {
                eligibleIds.add(permanent.getId());
            }
        }

        if (eligibleIds.isEmpty()) {
            return;
        }

        playerInputService.beginMultiPermanentChoice(
                gameData,
                controllerId,
                eligibleIds,
                Math.min(typedEffect.maxCount(), eligibleIds.size()),
                new MultiPermanentChoiceContext.OwnPermanentCounterPlacementOnChosenPermanents(
                        typedEffect.counterType(), 1, typedEffect.permanentFilter()),
                "Choose up to " + typedEffect.maxCount() + " permanents to put counters on.");
    }
}
