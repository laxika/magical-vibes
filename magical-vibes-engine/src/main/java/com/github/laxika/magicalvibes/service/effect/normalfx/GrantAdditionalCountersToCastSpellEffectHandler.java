package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalCountersToCastSpellEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves the additional-counter rider for the creature spell that caused a trigger.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GrantAdditionalCountersToCastSpellEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantAdditionalCountersToCastSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID spellCardId = entry.getTriggeringCardId();
        if (spellCardId == null) {
            return;
        }

        boolean spellStillOnStack = gameData.stack.stream()
                .anyMatch(stackEntry -> stackEntry.getCard() != null
                        && spellCardId.equals(stackEntry.getTargetableId()));
        if (!spellStillOnStack) {
            return;
        }

        int colorsSpent = gameData.getSpellCastColorsSpent(spellCardId).size();
        if (colorsSpent <= 0) {
            return;
        }

        gameData.spellAdditionalEnterCounters.merge(spellCardId, colorsSpent, Integer::sum);
        gameLogService.append(gameData, GameLog.text(
                "The triggering creature enters with " + colorsSpent + " additional +1/+1 counter(s)."));
        log.info("Game {} - triggering creature spell receives {} additional +1/+1 counter(s)",
                gameData.id, colorsSpent);
    }
}
