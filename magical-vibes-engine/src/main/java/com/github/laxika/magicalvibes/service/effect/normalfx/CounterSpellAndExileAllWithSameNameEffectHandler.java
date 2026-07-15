package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellAndExileAllWithSameNameEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
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
public class CounterSpellAndExileAllWithSameNameEffectHandler implements NormalEffectHandlerBean {

    private final CounterSupport counterSupport;
    private final GraveyardService graveyardService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterSpellAndExileAllWithSameNameEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) return;

        // Locate the target spell on the stack. If it's gone (illegal target), the whole spell fizzles.
        StackEntry targetEntry = null;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(targetCardId)) {
                targetEntry = se;
                break;
            }
        }
        if (targetEntry == null) {
            log.info("Game {} - Counterbore fizzles: target spell no longer on the stack", gameData.id);
            return;
        }

        String spellName = targetEntry.getCard().getName();
        UUID targetPlayerId = targetEntry.getControllerId();

        // Counter the spell if it can be countered. Even when it can't (uncounterable/protected),
        // the search-and-exile still happens per Counterbore's rulings.
        StackEntry counterable = counterSupport.findCounterTarget(gameData, targetCardId, entry);
        if (counterable != null) {
            counterSupport.counterSpell(gameData, entry, counterable);
        }

        // Search the spell's controller's graveyard, hand, and library for all cards with the same
        // name and exile them. (The countered spell itself is now in the graveyard, so it is caught.)
        List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        List<Card> library = gameData.playerDecks.get(targetPlayerId);

        int exiledFromGraveyard = exileMatching(gameData, targetPlayerId, graveyard, spellName);
        int exiledFromHand = exileMatching(gameData, targetPlayerId, hand, spellName);
        int exiledFromLibrary = exileMatching(gameData, targetPlayerId, library, spellName);

        if (exiledFromGraveyard > 0) {
            graveyardService.notifyCardsLeftGraveyard(gameData, targetPlayerId);
        }
        if (library != null) {
            Collections.shuffle(library);
        }

        String targetName = gameData.playerIdToName.get(targetPlayerId);
        int total = exiledFromGraveyard + exiledFromHand + exiledFromLibrary;
        String logEntry = "Counterbore counters " + spellName + " and exiles " + total
                + " card" + (total != 1 ? "s" : "") + " named " + spellName + " from " + targetName
                + "'s graveyard, hand, and library. " + targetName + " shuffles their library.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - Counterbore exiled {} cards named {} from {}'s zones",
                gameData.id, total, spellName, targetName);
    }

    private int exileMatching(GameData gameData, UUID playerId, List<Card> zone, String name) {
        if (zone == null) return 0;
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
