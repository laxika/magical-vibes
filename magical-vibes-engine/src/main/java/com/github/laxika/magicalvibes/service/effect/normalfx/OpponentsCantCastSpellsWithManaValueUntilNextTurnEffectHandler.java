package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsWithManaValueUntilNextTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class OpponentsCantCastSpellsWithManaValueUntilNextTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return OpponentsCantCastSpellsWithManaValueUntilNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Integer manaValue = entry.getEventValue();
        if (manaValue == null) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        Set<Integer> restrictedValues = gameData.opponentsCantCastSpellsWithManaValueUntilControllerNextTurn
                .computeIfAbsent(controllerId, ignored -> ConcurrentHashMap.newKeySet());
        restrictedValues.add(manaValue);
        gameLogService.append(gameData, GameLog.text(
                "Opponents can't cast spells with mana value " + manaValue + " until your next turn."));
    }
}
