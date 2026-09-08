package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.JeweledBirdAnteEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JeweledBirdAnteEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GraveyardService graveyardService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return JeweledBirdAnteEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        List<Card> sourceCards = source.cardsLeavingBattlefield();
        if (!permanentRemovalService.removePermanentToExile(gameData, source)) {
            return;
        }

        sourceCards.forEach(gameData::markAsAnted);
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), " is anted."));

        UUID sourceCardId = sourceCards.getFirst().getId();
        List<ExiledCardEntry> otherAntedCards = gameData.removeAntedCardsOwnedBy(
                entry.getControllerId(), sourceCardId);
        for (ExiledCardEntry anted : otherAntedCards) {
            graveyardService.addCardToGraveyard(gameData, entry.getControllerId(), anted.card(), Zone.EXILE);
            gameLogService.append(gameData,
                    GameLog.cardThen(anted.card(), " is put into your graveyard."));
        }

        playerInteractionSupport.applyDrawCards(gameData, entry.getControllerId(), 1);
        log.info("Game {} - {} antes {} and draws a card", gameData.id,
                gameData.playerIdToName.get(entry.getControllerId()), entry.getCard().getName());
    }
}
