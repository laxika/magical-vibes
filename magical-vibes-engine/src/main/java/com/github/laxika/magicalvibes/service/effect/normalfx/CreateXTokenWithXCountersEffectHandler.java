package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateXTokenWithXCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnCreatedPermanentsEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateXTokenWithXCountersEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;
    private final PermanentCounterSupport permanentCounterSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateXTokenWithXCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CreateXTokenWithXCountersEffect) effect;
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        if (e.entersWithCounters() && e.counterType() == CounterType.PLUS_ONE_PLUS_ONE) {
            int entryCounters = Math.max(0, amountEvaluationService.evaluate(
                    gameData, e.counterAmount(), AmountContext.forStackEntry(entry, source)));
            List<UUID> enteredIds = permanentControlSupport.applyCreateToken(
                    gameData, entry.getControllerId(),
                    e.tokenTemplate().withInitialPlusOnePlusOneCounters(entryCounters),
                    entry.getCard().getSetCode());
            entry.getCreatedPermanentIds().addAll(enteredIds);
            return;
        }
        List<UUID> createdIds = permanentControlSupport.applyCreateToken(
                gameData, entry.getControllerId(), e.tokenTemplate(), entry.getCard().getSetCode());
        entry.getCreatedPermanentIds().addAll(createdIds);
        if (createdIds.isEmpty() && gameData.interaction.isAwaitingInput()) {
            // Creation paused for a replacement choice (Jinnie Fay, Parallel Lives ordering). Whatever
            // token is created once it is answered receives the counters (they are never part of entering).
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, List.of(
                    new PutCountersOnCreatedPermanentsEffect(e.counterType(), e.counterAmount())));
            return;
        }
        // The token is created before X is counted, so a count of the controller's permanents
        // includes the new token (e.g. Gimbal, Gremlin Prodigy counts itself).
        int counterAmount = Math.max(0, amountEvaluationService.evaluate(
                gameData, e.counterAmount(), AmountContext.forStackEntry(entry, source)));
        if (counterAmount == 0 || createdIds.isEmpty()) {
            return;
        }

        for (UUID tokenId : createdIds) {
            Permanent token = gameQueryService.findPermanentById(gameData, tokenId);
            if (token == null || gameQueryService.cantHaveCounters(gameData, token)) {
                continue;
            }
            permanentCounterSupport.placeCounterOnPermanent(
                    gameData, entry, token, e.counterType(), counterAmount);
        }
    }
}
