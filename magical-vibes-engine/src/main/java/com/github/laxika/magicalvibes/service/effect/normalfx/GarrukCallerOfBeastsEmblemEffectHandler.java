package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GarrukCallerOfBeastsEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.SearchCreatureToBattlefieldOnControllerCastsCreatureSpellEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GarrukCallerOfBeastsEmblemEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GarrukCallerOfBeastsEmblemEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);

        Emblem emblem = new Emblem(controllerId, List.of(
                new SearchCreatureToBattlefieldOnControllerCastsCreatureSpellEffect.Marker()
        ), entry.getCard());

        gameData.emblems.add(emblem);

        String logEntry = playerName + " gets an emblem with \"Whenever you cast a creature spell, "
                + "you may search your library for a creature card, put it onto the battlefield, "
                + "then shuffle.\".";
        gameLogService.append(gameData, GameLog.text(logEntry));

        log.info("Game {} - {} gets Garruk, Caller of Beasts emblem", gameData.id, playerName);
    }
}
