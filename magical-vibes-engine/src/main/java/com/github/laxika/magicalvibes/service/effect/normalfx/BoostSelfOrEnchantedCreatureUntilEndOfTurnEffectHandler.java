package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostSelfOrEnchantedCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BoostSelfOrEnchantedCreatureUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostSelfOrEnchantedCreatureUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var boost = (BoostSelfOrEnchantedCreatureUntilEndOfTurnEffect) effect;
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        java.util.UUID targetId = source == null
                ? entry.getTargetId()
                : source.getAttachedTo() == null ? source.getId() : source.getAttachedTo();
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }

        AmountContext context = AmountContext.forStackEntry(entry, target);
        int powerBoost = amountEvaluationService.evaluate(gameData, boost.powerBoost(), context);
        int toughnessBoost = amountEvaluationService.evaluate(gameData, boost.toughnessBoost(), context);

        target.setPowerModifier(target.getPowerModifier() + powerBoost);
        target.setToughnessModifier(target.getToughnessModifier() + toughnessBoost);

        gameLogService.append(gameData, GameLog.builder()
                .card(target.getCard())
                .text(String.format(" gets %+d/%+d until end of turn.", powerBoost, toughnessBoost))
                .build());
        log.info("Game {} - {} gets {}/{} until end of turn", gameData.id,
                target.getCard().getName(), powerBoost, toughnessBoost);
    }
}
