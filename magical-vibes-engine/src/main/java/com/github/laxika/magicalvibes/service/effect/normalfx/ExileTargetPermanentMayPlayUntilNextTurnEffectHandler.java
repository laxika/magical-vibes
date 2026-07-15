package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetPermanentMayPlayUntilNextTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileSupport exileSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetPermanentMayPlayUntilNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        if (targetId == null && !entry.getTargetIds().isEmpty()) {
            targetId = entry.getTargetIds().getFirst();
        }
        if (targetId == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(entry.getCard().getName() + " fizzles (target no longer on the battlefield)."));
            return;
        }

        Card exiledCard = target.getOriginalCard();
        permanentRemovalService.removePermanentToExile(gameData, target);
        permanentRemovalService.removeOrphanedAuras(gameData);

        // A token exiled this way ceases to exist; nothing to play.
        UUID ownerId = gameQueryService.findExileOwnerById(gameData, exiledCard.getId());
        if (ownerId != null) {
            exileSupport.grantPlayUntilOwnersNextTurn(gameData, exiledCard.getId(), ownerId);
            String ownerName = gameData.playerIdToName.get(ownerId);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(exiledCard.getName() + " is exiled — " + ownerName
                            + " may play it until the end of their next turn."));
        } else {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(exiledCard.getName() + " is exiled."));
        }
        log.info("Game {} - {} exiled by {} (owner may play until next turn)",
                gameData.id, exiledCard.getName(), entry.getCard().getName());
    }
}
