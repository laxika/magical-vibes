package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEntersTappedToTriggeringCreatureSpellEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GrantEntersTappedToTriggeringCreatureSpellEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantEntersTappedToTriggeringCreatureSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getTriggeringCardId() == null) {
            return;
        }

        StackEntry spellEntry = gameQueryService.findStackEntryByCardId(
                gameData, entry.getTriggeringCardId());
        if (spellEntry == null) {
            return;
        }

        spellEntry.setEntersTapped(true);
        gameLogService.append(gameData, GameLog.cardThen(spellEntry.getCard(),
                " enters the battlefield tapped."));
    }
}
