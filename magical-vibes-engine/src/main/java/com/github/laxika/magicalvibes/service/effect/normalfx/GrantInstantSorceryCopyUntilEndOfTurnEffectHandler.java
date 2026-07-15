package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantInstantSorceryCopyUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrantInstantSorceryCopyUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantInstantSorceryCopyUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        gameData.playersWithSpellCopyUntilEndOfTurn.add(controllerId);

        String logMsg = gameData.playerIdToName.get(controllerId)
                + "'s instant and sorcery spells will be copied for the rest of the turn.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));
        log.info("Game {} - {} granted spell copy until end of turn", gameData.id, controllerId);
    }
}
