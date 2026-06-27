package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageToAnyTargetsEffect;
import java.util.UUID;
import java.util.Map;
import com.github.laxika.magicalvibes.model.Card;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealDividedDamageToAnyTargetsEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDividedDamageToAnyTargetsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDividedDamageToAnyTargetsEffect) effect;

        Map<UUID, Integer> assignments = gameData.pendingETBDamageAssignments;
        gameData.pendingETBDamageAssignments = Map.of();

        // dealDividedDamageToAnyTargets already calls checkWinCondition internally
        damageSupport.dealDividedDamageToAnyTargets(gameData, entry.getCard(), entry.getControllerId(), assignments);
    
    }
}
