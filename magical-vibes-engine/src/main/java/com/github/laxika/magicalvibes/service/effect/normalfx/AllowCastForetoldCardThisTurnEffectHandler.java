package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AllowCastForetoldCardThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AllowCastForetoldCardThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AllowCastForetoldCardThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        gameData.foretoldCardCastPermissionsThisTurn.add(
                new GameData.ForetoldCardCastPermission(controllerId, true));

        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(controllerId)
                + " may cast a foretold card they own from exile without paying its mana cost this turn."));
        log.info("Game {} - {} may cast a foretold card they own from exile without paying its mana cost this turn",
                gameData.id, gameData.playerIdToName.get(controllerId));
    }
}
