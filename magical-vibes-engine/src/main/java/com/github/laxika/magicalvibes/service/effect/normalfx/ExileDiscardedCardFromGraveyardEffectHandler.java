package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileDiscardedCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileDiscardedCardFromGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileDiscardedCardFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileDiscardedCardFromGraveyardEffect exileEffect = (ExileDiscardedCardFromGraveyardEffect) effect;
        if (exileEffect.trackWithSource() && entry.getSourcePermanentId() == null) {
            return;
        }
        if (!entry.getTriggeringCardIds().isEmpty()) {
            // One discard event: exile every card it discarded that is still the same graveyard object.
            for (UUID cardId : entry.getTriggeringCardIds()) {
                exileFromGraveyard(gameData, entry, exileEffect, cardId,
                        entry.getTargetCardGraveyardVersions().getOrDefault(cardId, -1L));
            }
            return;
        }
        if (entry.getTriggeringCardId() == null) {
            return;
        }
        exileFromGraveyard(gameData, entry, exileEffect, entry.getTriggeringCardId(),
                entry.getTriggeringCardGraveyardEntryVersion());
    }

    private void exileFromGraveyard(GameData gameData, StackEntry entry,
                                    ExileDiscardedCardFromGraveyardEffect exileEffect,
                                    UUID discardedCardId, long expectedVersion) {
        UUID ownerId = null;
        Card discarded = null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            discarded = graveyard.stream()
                    .filter(card -> discardedCardId.equals(card.getId()))
                    .findFirst()
                    .orElse(null);
            if (discarded != null) {
                ownerId = playerId;
                break;
            }
        }
        if (discarded == null) {
            return;
        }
        if (expectedVersion >= 0 && expectedVersion != gameData.graveyardEntryVersion(discarded.getId())) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, discarded.getId());
        if (exileEffect.addStashCounter()) {
            gameData.addToExileWithStashCounter(ownerId, discarded);
        } else if (exileEffect.trackWithSource()) {
            gameData.addToExile(ownerId, discarded, entry.getSourcePermanentId());
        } else {
            gameData.addToExile(ownerId, discarded);
        }
        if (exileEffect.grantPlayPermissionUntilEndOfTurn()) {
            gameData.exilePlayPermissions.put(discarded.getId(), entry.getControllerId());
            gameData.exilePlayPermissionsExpireEndOfTurn.add(discarded.getId());
        }
        gameLogService.append(gameData, GameLog.cardTextCard(entry.getCard(), " exiles ", discarded,
                " from " + gameData.playerIdToName.get(ownerId) + "'s graveyard."));
        log.info("Game {} - {} exiles discarded card {} from graveyard{}",
                gameData.id, entry.getCard().getName(), discarded.getName(),
                exileEffect.trackWithSource() ? " with the source" : "");
    }
}
