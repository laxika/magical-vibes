package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSacrificedPermanentWithSourceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Exiles the sacrificed card if it is still in its owner's graveyard. */
@Component
@RequiredArgsConstructor
public class ExileSacrificedPermanentWithSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileSacrificedPermanentWithSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileSacrificedPermanentWithSourceEffect exileEffect =
                (ExileSacrificedPermanentWithSourceEffect) effect;
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null && entry.getSourcePermanentSnapshot() != null) {
            sourcePermanentId = entry.getSourcePermanentSnapshot().getId();
        }
        Card sacrificed = exileEffect.sacrificedCard();
        if (sourcePermanentId == null || sacrificed == null) {
            return;
        }

        Card graveyardCard = gameQueryService.findCardInGraveyardById(gameData, sacrificed.getId());
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, sacrificed.getId());
        if (graveyardCard == null || ownerId == null) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, sacrificed.getId());
        gameData.addToExile(ownerId, graveyardCard, sourcePermanentId);
        gameLogService.append(gameData, GameLog.cardTextCard(entry.getCard(), " exiles ", graveyardCard,
                " from " + gameData.playerIdToName.get(ownerId) + "'s graveyard."));
    }
}
