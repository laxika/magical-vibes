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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Jeweled Bird's ante-and-draw ability. */
@Component
@RequiredArgsConstructor
@Slf4j
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
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null) {
            return;
        }

        List<Card> sourceCards = source.cardsLeavingBattlefield();
        Set<UUID> sourceCardIds = new HashSet<>();
        for (Card sourceCard : sourceCards) {
            sourceCardIds.add(sourceCard.getId());
        }

        if (!permanentRemovalService.removePermanentToExile(gameData, source)) {
            return;
        }

        for (Card sourceCard : sourceCards) {
            gameData.markCardAsAnted(sourceCard);
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
        if (!sourceCards.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(sourceCards.getFirst(), " is anted."));
        }

        moveOtherOwnedAnteCardsToGraveyard(gameData, entry.getControllerId(), sourceCardIds);
        playerInteractionSupport.applyDrawCards(gameData, entry.getControllerId(), 1);
    }

    private void moveOtherOwnedAnteCardsToGraveyard(GameData gameData, UUID controllerId,
                                                     Set<UUID> sourceCardIds) {
        for (ExiledCardEntry anted : List.copyOf(gameData.exiledCards)) {
            UUID cardId = anted.card().getId();
            if (!gameData.antedCardIds.contains(cardId)
                    || !controllerId.equals(anted.ownerId())
                    || sourceCardIds.contains(cardId)) {
                continue;
            }

            if (gameData.removeFromExile(cardId)) {
                graveyardService.addCardToGraveyard(gameData, anted.ownerId(), anted.card(), Zone.EXILE);
                gameLogService.append(gameData,
                        GameLog.cardThen(anted.card(), " is put into its owner's graveyard from the ante."));
                log.info("Game {} - {} moves {} from the ante to its owner's graveyard",
                        gameData.id, gameData.playerIdToName.get(controllerId), anted.card().getName());
            }
        }
    }
}
