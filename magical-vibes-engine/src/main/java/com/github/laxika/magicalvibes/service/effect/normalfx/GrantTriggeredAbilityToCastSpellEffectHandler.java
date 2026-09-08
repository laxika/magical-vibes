package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityToCastSpellEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GrantTriggeredAbilityToCastSpellEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantTriggeredAbilityToCastSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (GrantTriggeredAbilityToCastSpellEffect) effect;
        UUID spellCardId = entry.getTriggeringCardId();
        if (spellCardId == null) {
            return;
        }

        for (StackEntry spellEntry : gameData.stack) {
            if (spellEntry.getCard() != null && spellCardId.equals(spellEntry.getCard().getId())) {
                spellEntry.addGrantedTriggeredEffectOnEntry(grant.slot(), grant.grantedEffect());
                gameLogService.append(gameData, GameLog.cardThen(spellEntry.getCard(),
                        " gains a triggered ability as it enters the battlefield."));
                return;
            }
        }
    }
}
