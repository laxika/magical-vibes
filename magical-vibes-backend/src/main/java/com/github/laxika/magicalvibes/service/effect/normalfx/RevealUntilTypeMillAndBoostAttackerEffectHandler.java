package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilTypeMillAndBoostAttackerEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealUntilTypeMillAndBoostAttackerEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealUntilTypeMillAndBoostAttackerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RevealUntilTypeMillAndBoostAttackerEffect) effect;
        UUID controllerId = entry.getControllerId();
        UUID defenderId = gameQueryService.getOpponentId(gameData, controllerId);
        String defenderName = gameData.playerIdToName.get(defenderId);
        String sourceName = entry.getCard().getName();

        // Find the equipped creature via the source equipment's attachedTo
        Permanent equipment = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (equipment == null || equipment.getAttachedTo() == null) {
            log.info("Game {} - {} trigger fizzles: equipment no longer attached", gameData.id, sourceName);
            return;
        }
        Permanent equippedCreature = gameQueryService.findPermanentById(gameData, equipment.getAttachedTo());
        if (equippedCreature == null) {
            log.info("Game {} - {} trigger fizzles: equipped creature no longer on battlefield", gameData.id, sourceName);
            return;
        }

        List<Card> deck = gameData.playerDecks.get(defenderId);
        if (deck.isEmpty()) {
            String logEntry = defenderName + "'s library is empty — " + sourceName + "'s ability does nothing.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        // Reveal cards until a matching type is found
        List<Card> revealedCards = new ArrayList<>();
        while (!deck.isEmpty()) {
            Card card = deck.removeFirst();
            revealedCards.add(card);
            if (e.cardTypes().stream().anyMatch(card::hasType)) {
                break;
            }
        }

        // Log revealed cards
        String revealedNames = revealedCards.stream().map(Card::getName).collect(Collectors.joining(", "));
        String revealLog = defenderName + " reveals " + revealedNames + ".";
        gameBroadcastService.logAndBroadcast(gameData, revealLog);

        // Boost the equipped creature
        int revealedCount = revealedCards.size();
        int powerBoost = revealedCount * e.powerBoostPerCard();
        int toughnessBoost = revealedCount * e.toughnessBoostPerCard();
        if (powerBoost != 0 || toughnessBoost != 0) {
            equippedCreature.setPowerModifier(equippedCreature.getPowerModifier() + powerBoost);
            equippedCreature.setToughnessModifier(equippedCreature.getToughnessModifier() + toughnessBoost);

            String boostLog = equippedCreature.getCard().getName() + " gets +"
                    + powerBoost + "/+" + toughnessBoost + " until end of turn ("
                    + revealedCount + " " + (revealedCount != 1 ? "cards" : "card") + " revealed).";
            gameBroadcastService.logAndBroadcast(gameData, boostLog);
        }

        // Put all revealed cards into the graveyard
        for (Card card : revealedCards) {
            graveyardService.addCardToGraveyard(gameData, defenderId, card);
        }

        String millLog = defenderName + " puts " + revealedCount + " revealed "
                + (revealedCount != 1 ? "cards" : "card") + " into their graveyard.";
        gameBroadcastService.logAndBroadcast(gameData, millLog);

        log.info("Game {} - {} reveals {} cards from {}'s library, {} gets +{}/+{}",
                gameData.id, sourceName, revealedCount, defenderName,
                equippedCreature.getCard().getName(), powerBoost, toughnessBoost);
    }
}
