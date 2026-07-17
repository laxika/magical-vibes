package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAndTargetPlayerDiscardsEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SacrificeSelfAndTargetPlayerDiscardsEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeSelfAndTargetPlayerDiscardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SacrificeSelfAndTargetPlayerDiscardsEffect) effect;

        UUID targetPlayerId = entry.getTargetId();
        UUID sourcePermanentId = entry.getSourcePermanentId();

        if (targetPlayerId == null || sourcePermanentId == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(entry.getCard().getName() + "'s ability fizzles — source no longer on the battlefield."));
            return;
        }

        permanentRemovalService.removePermanentToGraveyard(gameData, source);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(entry.getCard().getName() + " is sacrificed."));

        String playerName = gameData.playerIdToName.get(targetPlayerId);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " must discard " + e.amount() + " card" + (e.amount() > 1 ? "s" : "")
                        + " (" + entry.getCard().getName() + ")."));

        playerInteractionSupport.resolveDiscardCards(gameData, targetPlayerId, e.amount());
    }
}
