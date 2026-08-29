package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalPlusOnePlusOneCounterToSnowPaidCreatureSpellEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves the snow-paid creature spell trigger by recording an as-enters counter grant on the
 * triggering spell while it remains on the stack.
 */
@Component
@RequiredArgsConstructor
public class GrantAdditionalPlusOnePlusOneCounterToSnowPaidCreatureSpellEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantAdditionalPlusOnePlusOneCounterToSnowPaidCreatureSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID spellCardId = entry.getTriggeringCardId();
        if (spellCardId == null) {
            return;
        }

        for (StackEntry spellEntry : gameData.stack) {
            if (spellEntry.getCard() == null || !spellCardId.equals(spellEntry.getCard().getId())) {
                continue;
            }
            if (spellEntry.getCard().getColors().stream()
                    .map(CardColor::name)
                    .map(ManaColor::valueOf)
                    .anyMatch(color -> gameData.getSpellCastSnowManaSpentByColor(spellCardId, color) > 0)) {
                gameData.spellAdditionalEnterCounters.merge(spellCardId, 1, Integer::sum);
                gameLogService.append(gameData, GameLog.cardThen(spellEntry.getCard(),
                        " enters with an additional +1/+1 counter."));
            }
            return;
        }
    }
}
