package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintDyingCreatureEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
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
    private final GameBroadcastService gameBroadcastService;
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

        // Find the source permanent (Mimic Vat) on the battlefield
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (sourcePermanent == null) {
            log.info("Game {} - Mimic Vat no longer on battlefield, imprint fizzles", gameData.id);
            return;
        }

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
        Card previouslyImprinted = gameData.getImprintedCard(sourcePermanent.getCard());
        if (previouslyImprinted != null) {
            // Find and remove from exile, tracking the owner
            var previousExiledEntry = gameData.findExiledCard(previouslyImprinted.getId());
            UUID previousOwnerId = previousExiledEntry != null ? previousExiledEntry.ownerId() : null;
            gameData.removeFromExile(previouslyImprinted.getId());
            // Return to owner's graveyard (the player whose exile zone it was in)
            UUID returnToId = previousOwnerId != null ? previousOwnerId : entry.getControllerId();
            graveyardService.addCardToGraveyard(gameData, returnToId, previouslyImprinted);
            String returnLog = previouslyImprinted.getName() + " returns to its owner's graveyard from exile.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(returnLog));
            log.info("Game {} - Previously imprinted {} returned to graveyard", gameData.id, previouslyImprinted.getName());
        }

        // Remove dying card from graveyard
        gameData.playerGraveyards.get(graveyardOwnerId).remove(dyingCard);
        graveyardService.notifyCardsLeftGraveyard(gameData, graveyardOwnerId);

        // Exile the dying card (add to card owner's exile zone)
        exileService.exileCard(gameData, graveyardOwnerId, dyingCard);

        // Set as imprinted on the source permanent
        gameData.setImprintedCard(sourcePermanent.getCard(), dyingCard);

        String logMsg = dyingCard.getName() + " is exiled and imprinted on " + sourcePermanent.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));
        log.info("Game {} - {} imprinted on {}", gameData.id, dyingCard.getName(), sourcePermanent.getCard().getName());
    }
}
