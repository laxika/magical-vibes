package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.SourceNextDamageToAnyTargetShield;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageBySelfEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves a fixed-source next-damage prevention shield. */
@Component
@RequiredArgsConstructor
public class PreventNextDamageBySelfEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventNextDamageBySelfEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourceId = entry.getSourcePermanentId();
        if (sourceId == null) {
            return;
        }

        gameData.sourceNextDamageToAnyTargetShields.add(new SourceNextDamageToAnyTargetShield(sourceId)
                .withRemainingDamage(((PreventNextDamageBySelfEffect) effect).amount()));
        gameLogService.append(gameData, GameLog.textCardText(
                "The next damage dealt by ", entry.getCard(), " this turn is reduced by "
                        + ((PreventNextDamageBySelfEffect) effect).amount() + "."));
    }
}
