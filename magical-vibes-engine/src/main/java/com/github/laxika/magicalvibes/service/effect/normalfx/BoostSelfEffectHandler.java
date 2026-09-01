package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.BuffTargetCreatureIndefinitelyEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
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
public class BoostSelfEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostSelfEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var boost = (BoostSelfEffect) effect;
        UUID selfId = entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetId();
        Permanent self = gameQueryService.findPermanentById(gameData, selfId);
        if (self == null) {
            return;
        }

        AmountContext ctx = AmountContext.forStackEntry(entry, self);
        int powerBoost = amountEvaluationService.evaluate(gameData, boost.powerBoost(), ctx);
        int toughnessBoost = amountEvaluationService.evaluate(gameData, boost.toughnessBoost(), ctx);

        if (boost.duration() == EffectDuration.UNTIL_END_OF_TURN) {
            self.setPowerModifier(self.getPowerModifier() + powerBoost);
            self.setToughnessModifier(self.getToughnessModifier() + toughnessBoost);
        } else {
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), entry.getCard().getName(), null, entry.getControllerId(),
                    new BuffTargetCreatureIndefinitelyEffect(powerBoost, toughnessBoost),
                    self.getId(), null, null, boost.duration(), 0));
        }

        gameLogService.append(gameData, GameLog.builder()
                .card(self.getCard())
                .text(String.format(" gets %+d/%+d %s.", powerBoost, toughnessBoost,
                        boost.duration() == EffectDuration.UNTIL_END_OF_COMBAT
                                ? "until end of combat" : "until end of turn"))
                .build());

        log.info("Game {} - {} gets {}/{}", gameData.id, self.getCard().getName(), powerBoost, toughnessBoost);
    }
}
