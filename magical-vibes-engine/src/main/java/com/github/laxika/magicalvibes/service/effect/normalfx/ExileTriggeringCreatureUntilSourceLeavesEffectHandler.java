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
    private final ReturnAllCardsExiledWithSourceEffectHandler returnHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTriggeringCreatureUntilSourceLeavesEffect.class;
    }

    /**
     * Exiles the triggering creature if it is still on the battlefield (Portcullis's ruling: if it isn't, the ability
     * does nothing) and links it to the source, which returns it when it leaves the battlefield. Portcullis's
     * intervening "if" is checked around this as the ability resolves (CR 603.4).
     */
    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        resolveExile(gameData, entry);
    }

    /**
     * Exiles the entering cast creature (if it is still on the battlefield) and then returns every
     * other permanent card exiled with the source. The exile is not tied to the source leaving, and the
     * return still happens when the entering creature or the source is already gone.
     */
    void resolveAndReturnOthers(GameData gameData, StackEntry entry) {
        UUID enteringPermanentId = entry.getTriggeringPermanentId();
        Permanent enteringPermanent = enteringPermanentId == null
                ? null : gameQueryService.findPermanentById(gameData, enteringPermanentId);
        UUID excludedCardId = null;
        if (enteringPermanent != null) {
            UUID sourcePermanentId = entry.getSourcePermanentId();
            Card card = enteringPermanent.getOriginalCard();
            UUID currentControllerId = gameQueryService.findPermanentController(gameData, enteringPermanentId);
            UUID ownerId = gameData.stolenCreatures.getOrDefault(enteringPermanentId, currentControllerId);

            permanentRemovalService.removePermanentToExile(gameData, enteringPermanent);
            gameLogService.append(gameData, GameLog.cardTextCard(card, " is exiled by ", entry.getCard(), "."));
            log.info("Game {} - {} exiles {}", gameData.id, entry.getCard().getName(), card.getName());

            var exiledEntry = gameData.findExiledCard(card.getId());
            if (sourcePermanentId != null && exiledEntry != null && exiledEntry.sourcePermanentId() == null) {
                gameData.removeFromExile(card.getId());
                gameData.addToExile(ownerId, card, sourcePermanentId);
            }
            excludedCardId = card.getId();
            permanentRemovalService.removeOrphanedAuras(gameData);
        }
        returnHandler.returnAllCardsExiledWithSourceExcept(gameData, entry, excludedCardId);
    }

    private void resolveExile(GameData gameData, StackEntry entry) {
        UUID enteringPermanentId = entry.getTriggeringPermanentId();
        Permanent enteringPermanent = enteringPermanentId == null
                ? null
                : gameQueryService.findPermanentById(gameData, enteringPermanentId);
        if (enteringPermanent == null) {
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
}
