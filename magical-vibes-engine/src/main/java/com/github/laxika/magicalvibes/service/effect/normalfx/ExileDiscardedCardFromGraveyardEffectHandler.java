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
        if (!exileEffect.trackWithSource() || entry.getTriggeringCardId() == null
                || entry.getSourcePermanentId() == null) {
            return;
        }

        UUID ownerId = entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.get(ownerId);
        if (graveyard == null) {
            return;
        }
        Card discarded = graveyard.stream()
                .filter(card -> entry.getTriggeringCardId().equals(card.getId()))
                .findFirst()
                .orElse(null);
        if (discarded == null) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, discarded.getId());
        gameData.addToExile(ownerId, discarded, entry.getSourcePermanentId());
        gameLogService.append(gameData, GameLog.cardTextCard(entry.getCard(), " exiles ", discarded,
                " from " + gameData.playerIdToName.get(ownerId) + "'s graveyard."));
        log.info("Game {} - {} exiles discarded card {} from graveyard with the source",
                gameData.id, entry.getCard().getName(), discarded.getName());
    }
}
