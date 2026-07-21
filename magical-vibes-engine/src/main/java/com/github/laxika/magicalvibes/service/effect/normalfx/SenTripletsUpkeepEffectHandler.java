package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SenTripletsUpkeepEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves Sen Triplets' upkeep ability against the chosen opponent: silences them, forbids ability
 * activation, and opens the {@link GameData#senControllerPlayerId}/{@link GameData#senControlledPlayerId}
 * window so the controller may play lands and cast spells from that opponent's (now revealed) hand.
 * All of this state is cleared at end of turn.
 */
@Component
@RequiredArgsConstructor
public class SenTripletsUpkeepEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SenTripletsUpkeepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        if (targetId == null) return;
        UUID controllerId = entry.getControllerId();

        gameData.playersSilencedThisTurn.add(targetId);
        gameData.playersCantActivateAbilitiesThisTurn.add(targetId);
        gameData.senControllerPlayerId = controllerId;
        gameData.senControlledPlayerId = targetId;

        String targetName = gameData.playerIdToName.get(targetId);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(targetName
                + " can't cast spells or activate abilities this turn and plays with their hand revealed."));
        gameBroadcastService.revealOpponentHandToPlayer(gameData, controllerId);
    }
}
