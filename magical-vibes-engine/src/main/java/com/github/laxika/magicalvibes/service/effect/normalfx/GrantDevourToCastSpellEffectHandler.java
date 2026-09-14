package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDevourToCastSpellEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Stamps a numeric devour grant onto the creature spell that caused the trigger. */
@Slf4j
@Component
@RequiredArgsConstructor
public class GrantDevourToCastSpellEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantDevourToCastSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (GrantDevourToCastSpellEffect) effect;
        UUID spellCardId = entry.getTriggeringCardId();
        if (spellCardId == null) {
            return;
        }

        for (StackEntry spellEntry : gameData.stack) {
            if (spellEntry.getCard() != null && spellCardId.equals(spellEntry.getCard().getId())) {
                spellEntry.setGrantedDevour(spellEntry.getGrantedDevour() + grant.multiplier());
                gameLogService.append(gameData, GameLog.cardThen(spellEntry.getCard(),
                        " gains devour " + grant.multiplier() + "."));
                log.info("Game {} - {} gains devour {}",
                        gameData.id, spellEntry.getCard().getName(), grant.multiplier());
                return;
            }
        }
    }
}
