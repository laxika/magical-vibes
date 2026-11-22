package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenThenPutCountersEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;

/** Creates the token and puts the counters on the tokens created by that same resolution. */
@Component
@RequiredArgsConstructor
public class CreateTokenThenPutCountersEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;
    private final PermanentCounterSupport permanentCounterSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenThenPutCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CreateTokenThenPutCountersEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        AmountContext context = AmountContext.forStackEntry(entry, source);
        // Every amount is read before any token is made (CR 608.2h).
        Map<UUID, Integer> counterAmounts = new LinkedHashMap<>();
        for (UUID playerId : subjectResolutionService.players(gameData, entry, effect,
                e.tokenTemplate().controller())) {
            counterAmounts.put(playerId,
                    amountEvaluationService.evaluate(gameData, e.amount(), context.withControllerId(playerId)));
        }
        counterAmounts.forEach((playerId, counterAmount) -> {
            List<UUID> createdIds = permanentControlSupport.applyCreateToken(
                    gameData, playerId, e.tokenTemplate(), entry.getCard().getSetCode());
            entry.getCreatedPermanentIds().addAll(createdIds);
            if (counterAmount <= 0) {
                return;
            }
            for (UUID createdId : createdIds) {
                Permanent token = gameQueryService.findPermanentById(gameData, createdId);
                if (token != null && !gameQueryService.cantHaveCounters(gameData, token)) {
                    // The player who creates the token is the one who puts the counters on it.
                    permanentCounterSupport.placeCounterOnPermanentForPlayer(
                            gameData, entry, token, e.counterType(), counterAmount, playerId);
                }
            }
        });
    }
}
