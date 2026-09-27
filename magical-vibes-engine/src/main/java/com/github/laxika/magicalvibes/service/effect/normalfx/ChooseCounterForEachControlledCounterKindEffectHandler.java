package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCounterForEachControlledCounterKindEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChooseCounterForEachControlledCounterKindEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseCounterForEachControlledCounterKindEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getSourcePermanentId() == null) {
            return;
        }

        List<CounterType> counterKinds = counterKindsAmongControlledPermanents(gameData, entry);
        if (!counterKinds.isEmpty()) {
            playerInputService.beginChooseCounterForEachControlledCounterKindChoice(
                    gameData, entry.getControllerId(), entry.getSourcePermanentId(),
                    entry.getCard().getName(), counterKinds);
        }
    }

    private List<CounterType> counterKindsAmongControlledPermanents(GameData gameData, StackEntry entry) {
        EnumSet<CounterType> counterKinds = EnumSet.noneOf(CounterType.class);
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(entry.getControllerId(), List.of())) {
            for (CounterType counterType : CounterType.values()) {
                if (counterType != CounterType.ANY && counterType != CounterType.SILVER
                        && permanent.getCounterCount(counterType) > 0) {
                    counterKinds.add(counterType);
                }
            }
        }
        return new ArrayList<>(counterKinds);
    }
}
