package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SetSelfBaseToughnessToAmountIndefinitelyEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SetSelfBaseToughnessToAmountIndefinitelyEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetSelfBaseToughnessToAmountIndefinitelyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SetSelfBaseToughnessToAmountIndefinitelyEffect) effect;
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        int toughness = amountEvaluationService.evaluate(gameData, e.toughness(),
                AmountContext.forStackEntry(entry, source));
        source.setBaseToughnessOverriddenPermanently(true);
        source.setPermanentBaseToughnessOverride(toughness);

        SetBasePowerToughnessEffect setter = new SetBasePowerToughnessEffect(
                null, toughness, GrantScope.SELF, EffectDuration.PERMANENT);
        FloatingContinuousEffect floating = gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), source.getId(), entry.getControllerId(),
                setter, source.getId(), null, null, EffectDuration.PERMANENT, 0));
        source.setPermanentBaseToughnessOverrideTimestamp(floating.timestamp());

        gameLogService.append(gameData, GameLog.builder().card(source.getCard())
                .text(" has base toughness " + toughness + " indefinitely.").build());
    }
}
