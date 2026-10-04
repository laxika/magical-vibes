package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChosenCountersFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RemoveChosenCountersFromTargetPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveChosenCountersFromTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        var removeEffect = (RemoveChosenCountersFromTargetPermanentEffect) effect;
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int amount = Math.max(0, amountEvaluationService.evaluate(gameData, removeEffect.amount(),
                AmountContext.forStackEntry(entry, source)));
        if (amount == 0) {
            return;
        }

        List<CounterType> counterTypes = counterTypesOn(target);
        if (!counterTypes.isEmpty()) {
            int available = counterTypes.stream().mapToInt(target::getCounterCount).sum();
            playerInputService.beginRemoveChosenCountersChoice(gameData, entry.getControllerId(),
                    target.getId(), entry.getCard().getName(),
                    Math.min(amount, available), counterTypes, removeEffect.exactAmount());
        }
    }

    public static List<CounterType> counterTypesOn(Permanent permanent) {
        List<CounterType> counterTypes = new ArrayList<>();
        for (CounterType counterType : CounterType.values()) {
            if (counterType != CounterType.ANY && counterType != CounterType.SILVER
                    && permanent.getCounterCount(counterType) > 0) {
                counterTypes.add(counterType);
            }
        }
        return counterTypes;
    }
}
