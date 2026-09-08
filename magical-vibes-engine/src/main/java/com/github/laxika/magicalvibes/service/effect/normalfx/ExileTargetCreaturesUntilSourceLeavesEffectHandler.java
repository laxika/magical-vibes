package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreaturesUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves the mixed-zone exile half of Angel of Serenity. The chosen ids ride on the trigger's
 * {@code targetCardIds} (they were picked from one pool spanning battlefields and graveyards), so
 * each one is looked up on the battlefield first and in the graveyards second. A target that has
 * since changed zones is simply skipped (CR 608.2b).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetCreaturesUntilSourceLeavesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetCreaturesUntilSourceLeavesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileTargetCreaturesUntilSourceLeavesEffect) effect;
        List<UUID> chosenCardIds = entry.getTargetCardIds();
        if (chosenCardIds.isEmpty()) {
            return;
        }

        // The source may have left in response to the trigger — then the cards are still exiled,
        // but nothing is left to return them.
        UUID sourcePermanentId = entry.getSourcePermanentId() != null
                && gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId()) != null
                ? entry.getSourcePermanentId() : null;

        for (UUID cardId : chosenCardIds) {
            Permanent permanent = findCreaturePermanentByCardId(gameData, cardId);
            if (permanent != null) {
                exileFromBattlefield(gameData, entry, e, sourcePermanentId, permanent);
                continue;
            }
            Card graveyardCard = gameQueryService.findCardInGraveyardById(gameData, cardId);
            if (graveyardCard != null) {
                exileFromGraveyard(gameData, entry, e, sourcePermanentId, cardId, graveyardCard);
            }
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    private void exileFromBattlefield(GameData gameData, StackEntry entry,
            ExileTargetCreaturesUntilSourceLeavesEffect e, UUID sourcePermanentId, Permanent permanent) {
        Card card = permanent.getOriginalCard();
        UUID controllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
        UUID ownerId = gameData.stolenCreatures.getOrDefault(permanent.getId(), controllerId);
        boolean token = permanent.getCard().isToken();

        permanentRemovalService.removePermanentToExile(gameData, permanent);
        gameLogService.append(gameData, GameLog.cardTextCard(card, " is exiled by ", entry.getCard(), "."));

        // A token that leaves the battlefield ceases to exist (CR 111.7) — nothing to return.
        if (sourcePermanentId != null && !token) {
            registerReturn(gameData, e, sourcePermanentId, card, ownerId);
        }
    }

    private void exileFromGraveyard(GameData gameData, StackEntry entry,
            ExileTargetCreaturesUntilSourceLeavesEffect e, UUID sourcePermanentId, UUID cardId, Card card) {
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, cardId);
        exileService.exileCard(gameData, ownerId, card, sourcePermanentId);
        gameLogService.append(gameData, GameLog.cardTextCard(card, " is exiled from a graveyard by ",
                entry.getCard(), "."));

        if (sourcePermanentId != null) {
            registerReturn(gameData, e, sourcePermanentId, card, ownerId);
        }
    }

    private void registerReturn(GameData gameData, ExileTargetCreaturesUntilSourceLeavesEffect e,
            UUID sourcePermanentId, Card card, UUID ownerId) {
        gameData.addExileReturnOnPermanentLeave(sourcePermanentId,
                new PendingExileReturn(card, ownerId, false, e.returnToHand()));
        log.info("Game {} - {} exiled until its exiler leaves the battlefield", gameData.id, card.getName());
    }

    private Permanent findCreaturePermanentByCardId(GameData gameData, UUID cardId) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                if (permanent.getCard().getId().equals(cardId)) {
                    return permanent;
                }
            }
        }
        return null;
    }
}
