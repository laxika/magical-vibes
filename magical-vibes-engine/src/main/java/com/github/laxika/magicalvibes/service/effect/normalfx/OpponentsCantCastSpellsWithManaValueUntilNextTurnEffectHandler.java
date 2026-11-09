package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsWithManaValueUntilNextTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class OpponentsCantCastSpellsWithManaValueUntilNextTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return OpponentsCantCastSpellsWithManaValueUntilNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        // "That spell's mana value": the triggering spell's, as it last existed on the stack once it has left
        // (CR 608.2h).
        int manaValue = amountEvaluationService.evaluate(gameData, new TargetSpellManaValue(),
                AmountContext.forStackEntry(entry, null));

        UUID controllerId = entry.getControllerId();
        Set<Integer> restrictedValues = gameData.opponentsCantCastSpellsWithManaValueUntilControllerNextTurn
                .computeIfAbsent(controllerId, ignored -> ConcurrentHashMap.newKeySet());
        restrictedValues.add(manaValue);
        gameLogService.append(gameData, GameLog.text(
                "Opponents can't cast spells with mana value " + manaValue + " until your next turn."));
    }
}
