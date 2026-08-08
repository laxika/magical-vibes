package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndAllWithSameNameFromZonesEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetPermanentAndAllWithSameNameFromZonesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GraveyardService graveyardService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetPermanentAndAllWithSameNameFromZonesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        if (targetId == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            log.info("Game {} - {} fizzles: target permanent is gone", gameData.id, entry.getCard().getName());
            return;
        }

        String name = target.getCard().getName();
        UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());

        permanentRemovalService.removePermanentToExile(gameData, target);
        gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " is exiled."));
        permanentRemovalService.removeOrphanedAuras(gameData);

        if (controllerId == null) {
            return;
        }

        int fromGraveyard = exileMatching(gameData, controllerId, gameData.playerGraveyards.get(controllerId), name);
        int fromHand = exileMatching(gameData, controllerId, gameData.playerHands.get(controllerId), name);
        List<Card> library = gameData.playerDecks.get(controllerId);
        int fromLibrary = exileMatching(gameData, controllerId, library, name);

        if (fromGraveyard > 0) {
            graveyardService.notifyCardsLeftGraveyard(gameData, controllerId);
        }
        if (library != null) {
            Collections.shuffle(library);
        }

        String controllerName = gameData.playerIdToName.get(controllerId);
        int total = fromGraveyard + fromHand + fromLibrary;
        gameLogService.append(gameData, GameLog.text(entry.getCard().getName() + " exiles " + total
                + " card" + (total != 1 ? "s" : "") + " named " + name + " from " + controllerName
                + "'s graveyard, hand, and library. " + controllerName + " shuffles their library."));
        log.info("Game {} - {} exiled {} cards named {} from {}'s zones",
                gameData.id, entry.getCard().getName(), total, name, controllerName);
    }

    private int exileMatching(GameData gameData, UUID playerId, List<Card> zone, String name) {
        if (zone == null) {
            return 0;
        }
        List<Card> matches = new ArrayList<>();
        for (Card card : zone) {
            if (card.getName().equals(name)) {
                matches.add(card);
            }
        }
        zone.removeAll(matches);
        for (Card card : matches) {
            gameData.addToExile(playerId, card);
        }
        return matches.size();
    }
}
