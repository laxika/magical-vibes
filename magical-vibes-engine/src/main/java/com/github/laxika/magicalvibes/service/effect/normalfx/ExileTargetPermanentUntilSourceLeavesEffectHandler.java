package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
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
public class ExileTargetPermanentUntilSourceLeavesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetPermanentUntilSourceLeavesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileTargetPermanentUntilSourceLeavesEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        UUID sourcePermanentId = entry.getSourcePermanentId();
        Permanent sourcePermanent = sourcePermanentId == null
                ? null
                : gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (sourcePermanent == null) {
            sourcePermanentId = null;
            UUID controllerId = entry.getControllerId();
            List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
            if (battlefield != null) {
                for (Permanent p : battlefield) {
                    if (p.getCard() == entry.getCard()) {
                        sourcePermanentId = p.getId();
                        sourcePermanent = p;
                        break;
                    }
                }
            }
        }

        Card card = target.getOriginalCard();
        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        UUID ownerId = gameData.stolenCreatures.getOrDefault(target.getId(), targetControllerId);

        permanentRemovalService.removePermanentToExile(gameData, target);

        if (e.imprint() && sourcePermanent != null) {
            gameData.setImprintedCard(sourcePermanent.getCard(), card);
        }

        gameLogService.append(gameData, GameLog.cardTextCard(card, " is exiled by ", entry.getCard(), "."));
        log.info("Game {} - {} exiles {} until it leaves the battlefield",
                gameData.id, entry.getCard().getName(), card.getName());

        if (!card.isToken() && sourcePermanent != null) {
            gameData.addExileReturnOnPermanentLeave(sourcePermanentId, new PendingExileReturn(card, ownerId));

            var exiledEntry = gameData.findExiledCard(card.getId());
            if (exiledEntry != null && exiledEntry.sourcePermanentId() == null) {
                gameData.removeFromExile(card.getId());
                gameData.addToExile(ownerId, card, sourcePermanentId);
            }
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
