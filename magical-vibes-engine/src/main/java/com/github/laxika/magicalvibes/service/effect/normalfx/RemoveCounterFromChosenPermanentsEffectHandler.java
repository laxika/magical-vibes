package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromChosenPermanentsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a controller's resolution-time choice of permanents from which counters are removed. */
@Component
@RequiredArgsConstructor
public class RemoveCounterFromChosenPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveCounterFromChosenPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var typedEffect = (RemoveCounterFromChosenPermanentsEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        List<UUID> eligibleIds = new ArrayList<>();
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(
                        gameData, permanent, typedEffect.permanentFilter())) {
                    eligibleIds.add(permanent.getId());
                }
            }
        }

        if (eligibleIds.isEmpty()) {
            entry.setEventValue(0);
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(controllerId) + " has no matching permanents."));
            return;
        }

        playerInputService.beginMultiPermanentChoice(
                gameData,
                controllerId,
                eligibleIds,
                eligibleIds.size(),
                new MultiPermanentChoiceContext.RemoveCounterFromChosenPermanents(
                        entry, typedEffect.counterType(), typedEffect.permanentFilter()),
                "Choose any number of permanents from which to remove a counter.");
    }
}
