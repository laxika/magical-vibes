package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetPermanentAndTrackWithSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetPermanentAndTrackWithSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        Card exiledCard = target.getOriginalCard();
        permanentRemovalService.removePermanentToExile(gameData, target);

        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            UUID controllerId = entry.getControllerId();
            List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
            if (battlefield != null) {
                for (Permanent p : battlefield) {
                    if (p.getCard() == entry.getCard()) {
                        sourcePermanentId = p.getId();
                        break;
                    }
                }
            }
        }

        if (sourcePermanentId != null) {
            // removePermanentToExile already added to exile without source tracking;
            // remove that entry and re-add with source tracking
            var exiledEntry = gameData.findExiledCard(exiledCard.getId());
            UUID ownerId = exiledEntry != null ? exiledEntry.ownerId() : entry.getControllerId();
            gameData.removeFromExile(exiledCard.getId());
            gameData.addToExile(ownerId, exiledCard, sourcePermanentId);
        }

        String logEntry = exiledCard.getName() + " is exiled by " + entry.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} exiles {} (tracked with source)",
                gameData.id, entry.getCard().getName(), exiledCard.getName());

        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
