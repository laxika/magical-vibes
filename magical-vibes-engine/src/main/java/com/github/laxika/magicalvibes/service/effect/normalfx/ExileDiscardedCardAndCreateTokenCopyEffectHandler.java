package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileDiscardedCardAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves a copy effect for the exact card chosen by a preceding discard. */
@Component
@RequiredArgsConstructor
public class ExileDiscardedCardAndCreateTokenCopyEffectHandler implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final TokenCopySupport tokenCopySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileDiscardedCardAndCreateTokenCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileDiscardedCardAndCreateTokenCopyEffect copyEffect =
                (ExileDiscardedCardAndCreateTokenCopyEffect) effect;
        UUID discardedCardId = entry.getTriggeringCardId();
        if (discardedCardId == null) {
            return;
        }

        UUID graveyardOwnerId = null;
        Card discardedCard = null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) {
                continue;
            }
            discardedCard = graveyard.stream()
                    .filter(card -> discardedCardId.equals(card.getId()))
                    .findFirst()
                    .orElse(null);
            if (discardedCard != null) {
                graveyardOwnerId = playerId;
                break;
            }
        }
        if (discardedCard == null || graveyardOwnerId == null) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, discardedCardId);
        exileService.exileCard(gameData, graveyardOwnerId, discardedCard);
        gameLogService.append(gameData, GameLog.cardTextCard(entry.getCard(), " exiles ", discardedCard,
                " from " + gameData.playerIdToName.get(graveyardOwnerId) + "'s graveyard."));

        Permanent sourcePermanent = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        tokenCopySupport.createTokenCopies(gameData, entry, List.of(discardedCard), sourcePermanent,
                copyEffect.tokenCopyEffect());
    }
}
