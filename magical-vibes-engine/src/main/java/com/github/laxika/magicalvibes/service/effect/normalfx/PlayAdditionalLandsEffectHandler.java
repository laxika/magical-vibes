package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayAdditionalLandsEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlayAdditionalLandsEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PlayAdditionalLandsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PlayAdditionalLandsEffect) effect;
        UUID controllerId = entry.getControllerId();
        gameData.additionalLandsThisTurn.merge(controllerId, e.count(), Integer::sum);

        String controllerName = gameData.playerIdToName.get(controllerId);
        String logEntry = controllerName + " may play up to " + e.count() + " additional land"
                + (e.count() == 1 ? "" : "s") + " this turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} may play {} additional lands this turn", gameData.id, controllerName, e.count());
    }
}
