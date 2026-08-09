package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCreatureUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTriggeringCreatureUntilSourceLeavesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTriggeringCreatureUntilSourceLeavesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exileEffect = (ExileTriggeringCreatureUntilSourceLeavesEffect) effect;
        UUID enteringPermanentId = entry.getTriggeringPermanentId();
        Permanent enteringPermanent = enteringPermanentId == null
                ? null
                : gameQueryService.findPermanentById(gameData, enteringPermanentId);
        if (enteringPermanent == null
                || countOtherCreatures(gameData, enteringPermanentId) < exileEffect.minimumOtherCreatures()) {
            return;
        }

        UUID sourcePermanentId = entry.getSourcePermanentId();
        boolean sourceOnBattlefield = sourcePermanentId != null
                && gameQueryService.findPermanentById(gameData, sourcePermanentId) != null;
        Card card = enteringPermanent.getOriginalCard();
        UUID currentControllerId = gameQueryService.findPermanentController(gameData, enteringPermanentId);
        UUID ownerId = gameData.stolenCreatures.getOrDefault(enteringPermanentId, currentControllerId);

        permanentRemovalService.removePermanentToExile(gameData, enteringPermanent);
        gameLogService.append(gameData, GameLog.cardTextCard(card, " is exiled by ", entry.getCard(), "."));
        log.info("Game {} - {} exiles {} until it leaves the battlefield",
                gameData.id, entry.getCard().getName(), card.getName());

        if (sourceOnBattlefield && !enteringPermanentId.equals(sourcePermanentId)) {
            gameData.addExileReturnOnPermanentLeave(sourcePermanentId, new PendingExileReturn(card, ownerId));

            var exiledEntry = gameData.findExiledCard(card.getId());
            if (exiledEntry != null && exiledEntry.sourcePermanentId() == null) {
                gameData.removeFromExile(card.getId());
                gameData.addToExile(ownerId, card, sourcePermanentId);
            }
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    private int countOtherCreatures(GameData gameData, UUID enteringPermanentId) {
        int[] count = {0};
        gameData.forEachPermanent((playerId, permanent) -> {
            if (!permanent.getId().equals(enteringPermanentId)
                    && gameQueryService.isCreature(gameData, permanent)) {
                count[0]++;
            }
        });
        return count[0];
    }
}
