package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageToTargetAndAddPlusOnePlusOneCountersEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PreventNextDamageToTargetAndAddPlusOnePlusOneCountersEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventNextDamageToTargetAndAddPlusOnePlusOneCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var prevent = (PreventNextDamageToTargetAndAddPlusOnePlusOneCountersEffect) effect;
        UUID targetId = entry.getTargetId();
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) return;

        int amount = amountEvaluationService.evaluate(
                gameData, prevent.amount(), AmountContext.forStackEntry(entry, null));
        target.setDamageToPlusOnePlusOneCounterPreventionShield(
                target.getDamageToPlusOnePlusOneCounterPreventionShield() + amount);
        gameLogService.append(gameData, GameLog.builder()
                .text("The next " + amount + " damage that would be dealt to ")
                .card(target.getCard())
                .text(" is prevented; +1/+1 counters are put on it for damage prevented this way.")
                .build());
        log.info("Game {} - Prevention-to-+1/+1-counters shield {} added to permanent {}",
                gameData.id, amount, target.getCard().getName());
    }
}
