package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToNextSpellOfTypeThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrantFlashToNextSpellOfTypeThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantFlashToNextSpellOfTypeThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        GrantFlashToNextSpellOfTypeThisTurnEffect grant = (GrantFlashToNextSpellOfTypeThisTurnEffect) effect;
        gameData.addNextSpellFlashGrant(entry.getControllerId(), grant.cardType());
        String typeName = grant.cardType().name().toLowerCase(Locale.ROOT);
        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" lets its controller cast their next " + typeName
                        + " spell this turn as though it had flash.")
                .build());
        log.info("Game {} - {} grants flash to the next {} spell for player {} this turn",
                gameData.id, entry.getCard().getName(), typeName, entry.getControllerId());
    }
}
