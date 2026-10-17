package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintDyingCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImprintDyingCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final ExileService exileService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ImprintDyingCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ImprintDyingCreatureEffect) effect;
        UUID dyingCardId = e.dyingCardId();
        if (dyingCardId == null) return;

        // The ability resolves even if the source left the battlefield: the card is still exiled and the
        // previously exiled cards still return, but the new card is no longer linked to a permanent.
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        Card sourceCard = sourcePermanent != null ? sourcePermanent.getCard() : entry.getCard();

        // Find the dying card in any graveyard
        Card dyingCard = null;
        UUID graveyardOwnerId = null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card c : graveyard) {
                if (c.getId().equals(dyingCardId)) {
                    dyingCard = c;
                    graveyardOwnerId = playerId;
                    break;
                }
            }
            if (dyingCard != null) break;
        }

        if (dyingCard == null) {
            log.info("Game {} - Dying card no longer in any graveyard, imprint fizzles", gameData.id);
            return;
        }

        // Return previously imprinted card to its owner's graveyard
        Card previouslyImprinted = gameData.getImprintedCard(sourceCard);
        if (previouslyImprinted != null) {
            // Find and remove from exile, tracking the owner
            var previousExiledEntry = gameData.findExiledCard(previouslyImprinted.getId());
            UUID previousOwnerId = previousExiledEntry != null ? previousExiledEntry.ownerId() : null;
            gameData.removeFromExile(previouslyImprinted.getId());
            // Return to owner's graveyard (the player whose exile zone it was in)
            UUID returnToId = previousOwnerId != null ? previousOwnerId : entry.getControllerId();
            graveyardService.addCardToGraveyard(gameData, returnToId, previouslyImprinted);
            gameLogService.append(gameData, GameLog.cardThen(previouslyImprinted, " returns to its owner's graveyard from exile."));
            log.info("Game {} - Previously imprinted {} returned to graveyard", gameData.id, previouslyImprinted.getName());
        }

        // Remove dying card from graveyard
        gameData.playerGraveyards.get(graveyardOwnerId).remove(dyingCard);
        graveyardService.notifyCardsExiledFromGraveyard(gameData, graveyardOwnerId, dyingCard);

        // Exile the dying card (add to card owner's exile zone)
        exileService.exileCard(gameData, graveyardOwnerId, dyingCard);

        if (sourcePermanent == null) {
            gameData.setImprintedCard(sourceCard, null);
            gameLogService.append(gameData, GameLog.cardTextCard(dyingCard, " is exiled by ", sourceCard, "."));
            return;
        }

        // Set as imprinted on the source permanent
        gameData.setImprintedCard(sourceCard, dyingCard);

        gameLogService.append(gameData, GameLog.cardTextCard(dyingCard, " is exiled and imprinted on ", sourceCard, "."));
        log.info("Game {} - {} imprinted on {}", gameData.id, dyingCard.getName(), sourceCard.getName());
    }
}
