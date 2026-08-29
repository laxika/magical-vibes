package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellAndExileAllWithSameNameEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
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
    private final DrawService drawService;
    private final PlayerInputService playerInputService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterSpellAndExileAllWithSameNameEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) return;
        CounterSpellAndExileAllWithSameNameEffect counterEffect =
                (CounterSpellAndExileAllWithSameNameEffect) effect;

        // Locate the target spell on the stack. If it's gone (illegal target), the whole spell fizzles.
        StackEntry targetEntry = null;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(targetCardId)) {
                targetEntry = se;
                break;
            }
        }
        if (targetEntry == null) {
            log.info("Game {} - {} fizzles: target spell no longer on the stack", gameData.id,
                    entry.getCard().getName());
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

        if (counterEffect.chooseAnyNumber()) {
            beginAnyNumberChoice(gameData, entry, targetPlayerId, spellName, counterEffect);
            return;
        }

        // Search the spell's controller's graveyard, hand, and library for all cards with the same
        // name and exile them. (The countered spell itself is now in the graveyard, so it is caught.)
        List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        List<Card> library = gameData.playerDecks.get(targetPlayerId);

        List<Card> exiledGraveyardCards = exileMatching(gameData, targetPlayerId, graveyard, spellName);
        int exiledFromGraveyard = exiledGraveyardCards.size();
        int exiledFromHand = exileMatching(gameData, targetPlayerId, hand, spellName).size();
        int exiledFromLibrary = exileMatching(gameData, targetPlayerId, library, spellName).size();

        if (exiledFromGraveyard > 0) {
            graveyardService.notifyCardsExiledFromGraveyard(gameData, targetPlayerId, exiledGraveyardCards);
        }
        if (library != null) {
            Collections.shuffle(library);
        }

        if (counterEffect.drawCardsExiledFromHand()) {
            for (int i = 0; i < exiledFromHand; i++) {
                drawService.resolveDrawCard(gameData, targetPlayerId);
            }
        }

        String targetName = gameData.playerIdToName.get(targetPlayerId);
        int total = exiledFromGraveyard + exiledFromHand + exiledFromLibrary;
        String logEntry = entry.getCard().getName() + " counters " + spellName + " and exiles " + total
                + " card" + (total != 1 ? "s" : "") + " named " + spellName + " from " + targetName
                + "'s graveyard, hand, and library. " + targetName + " shuffles their library.";
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} exiled {} cards named {} from {}'s zones",
                gameData.id, entry.getCard().getName(), total, spellName, targetName);
    }

    private void beginAnyNumberChoice(GameData gameData, StackEntry entry, UUID targetPlayerId,
                                      String spellName,
                                      CounterSpellAndExileAllWithSameNameEffect effect) {
        List<Card> matchingCards = collectMatchingCards(gameData, targetPlayerId, spellName);
        List<Card> library = gameData.playerDecks.get(targetPlayerId);
        if (matchingCards.isEmpty()) {
            if (library != null) {
                Collections.shuffle(library);
            }
            return;
        }

        playerInputService.beginMultiZoneExileChoice(
                gameData, entry.getControllerId(), matchingCards, targetPlayerId, spellName,
                effect.drawCardsExiledFromHand());
    }

    private List<Card> collectMatchingCards(GameData gameData, UUID playerId, String name) {
        List<Card> matchingCards = new ArrayList<>();
        addMatchingCards(matchingCards, gameData.playerGraveyards.get(playerId), name);
        addMatchingCards(matchingCards, gameData.playerHands.get(playerId), name);
        addMatchingCards(matchingCards, gameData.playerDecks.get(playerId), name);
        return matchingCards;
    }

    private void addMatchingCards(List<Card> matchingCards, List<Card> cards, String name) {
        if (cards != null) {
            matchingCards.addAll(cards.stream().filter(card -> card.getName().equals(name)).toList());
        }
    }

    private List<Card> exileMatching(GameData gameData, UUID playerId, List<Card> zone, String name) {
        if (zone == null) return List.of();
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
        return matches;
    }
}
