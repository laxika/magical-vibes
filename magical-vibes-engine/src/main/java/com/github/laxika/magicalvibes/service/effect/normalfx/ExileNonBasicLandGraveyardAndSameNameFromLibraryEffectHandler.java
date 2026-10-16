package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileNonBasicLandGraveyardAndSameNameFromLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileNonBasicLandGraveyardAndSameNameFromLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final LibrarySearchSupport librarySearchSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileNonBasicLandGraveyardAndSameNameFromLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        UUID targetPlayerId = entry.getTargetId();
        UUID controllerId = entry.getControllerId();
        String controllerName = gameData.playerIdToName.get(controllerId);

        // Fizzle if no valid target player (e.g. target was illegal on resolution)
        if (targetPlayerId == null || !gameData.playerGraveyards.containsKey(targetPlayerId)) {
            log.warn("Game {} - Haunting Echoes fizzles: no valid target player", gameData.id);
            return;
        }

        String targetName = gameData.playerIdToName.get(targetPlayerId);

        List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);
        List<Card> library = gameData.playerDecks.get(targetPlayerId);

        // Separate basic land cards from non-basic-land cards in the graveyard
        List<Card> toExile = new ArrayList<>();
        for (Card card : graveyard) {
            boolean isBasicLand = card.hasType(CardType.LAND)
                    && card.getSupertypes().contains(CardSupertype.BASIC);
            if (!isBasicLand) {
                toExile.add(card);
            }
        }

        if (toExile.isEmpty()) {
            // No non-basic-land cards to exile — just shuffle and log
            java.util.Collections.shuffle(library);
            String logEntry = controllerName + " resolves Haunting Echoes — no non-basic-land cards in "
                    + targetName + "'s graveyard. " + targetName + " shuffles their library.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - Haunting Echoes: no non-basic-land cards in {}'s graveyard", gameData.id, targetName);
            return;
        }

        // Exile all non-basic-land cards from graveyard
        graveyard.removeAll(toExile);
        for (Card card : toExile) {
            gameData.addToExile(targetPlayerId, card);
        }
        graveyardService.notifyCardsExiledFromGraveyard(gameData, targetPlayerId, toExile);

        // Collect unique card names from the exiled graveyard cards
        Set<String> exiledNames = new java.util.LinkedHashSet<>();
        for (Card card : toExile) {
            exiledNames.add(card.getName());
        }

        // The search is for cards with a stated quality, so the searching player isn't required to
        // find all of them (CR 701.23b); the caster chooses which matching library cards to exile.
        List<Card> libraryMatches = new ArrayList<>();
        boolean librarySearchAllowed = !librarySearchSupport.isSearchPrevented(
                gameData, controllerId, targetPlayerId, false, controllerId);
        for (Card card : library) {
            if (librarySearchAllowed && exiledNames.contains(card.getName())) {
                libraryMatches.add(card);
            }
        }

        String logEntry = controllerName + " resolves Haunting Echoes — exiles " + toExile.size()
                + " card" + (toExile.size() != 1 ? "s" : "") + " from " + targetName + "'s graveyard.";
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - Haunting Echoes: exiled {} from graveyard of {}",
                gameData.id, toExile.size(), targetName);

        if (!libraryMatches.isEmpty()) {
            playerInputService.beginMultiZoneExileChoice(gameData, controllerId, libraryMatches,
                    targetPlayerId, String.join(", ", exiledNames));
            return;
        }

        java.util.Collections.shuffle(library);
        gameLogService.append(gameData, GameLog.text(targetName + " shuffles their library."));
    }
}
