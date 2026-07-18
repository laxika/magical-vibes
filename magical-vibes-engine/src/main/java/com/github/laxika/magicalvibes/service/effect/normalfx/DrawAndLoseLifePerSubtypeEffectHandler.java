package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawAndLoseLifePerSubtypeEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DrawAndLoseLifePerSubtypeEffectHandler implements NormalEffectHandlerBean {

    private final DrawService drawService;
    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawAndLoseLifePerSubtypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DrawAndLoseLifePerSubtypeEffect) effect;

        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        int count = 0;
        if (battlefield != null) {
            for (Permanent perm : battlefield) {
                if (perm.getCard().getSubtypes().contains(e.subtype())) {
                    count++;
                }
            }
        }

        if (count == 0) {
            String logEntry = playerName + " controls no " + e.subtype().getDisplayName() + "s — draws nothing and loses no life.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} controls no {}s for draw/life loss", gameData.id, playerName, e.subtype().getDisplayName());
            return;
        }

        for (int i = 0; i < count; i++) {
            drawService.resolveDrawCard(gameData, controllerId);
        }

        if (!gameQueryService.canPlayerLifeChange(gameData, controllerId)) {
            String logEntry = playerName + " draws " + count + " card" + (count != 1 ? "s" : "")
                    + " (" + entry.getCard().getName() + "). " + playerName + "'s life total can't change.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().text(playerName + " draws " + count + " card" + (count != 1 ? "s" : "") + " (").card(entry.getCard()).text("). " + playerName + "'s life total can't change.").build());
        } else {
            int currentLife = gameData.getLife(controllerId);
            gameData.playerLifeTotals.put(controllerId, currentLife - count);

            String logEntry = playerName + " draws " + count + " card" + (count != 1 ? "s" : "")
                    + " and loses " + count + " life (" + entry.getCard().getName() + ").";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().text(playerName + " draws " + count + " card" + (count != 1 ? "s" : "") + " and loses " + count + " life (").card(entry.getCard()).text(").").build());
            log.info("Game {} - {} draws {} and loses {} life from {}", gameData.id, playerName, count, count, entry.getCard().getName());
        }
    
    }
}
