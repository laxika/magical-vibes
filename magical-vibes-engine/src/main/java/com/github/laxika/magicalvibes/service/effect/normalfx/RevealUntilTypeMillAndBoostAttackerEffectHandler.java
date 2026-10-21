package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilTypeMillAndBoostAttackerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
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
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealUntilTypeMillAndBoostAttackerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RevealUntilTypeMillAndBoostAttackerEffect) effect;
        UUID defenderId = entry.getDefendingPlayerId();
        if (defenderId == null) {
            return;
        }
        String defenderName = gameData.playerIdToName.get(defenderId);
        String sourceName = entry.getCard().getName();

        // The trigger belongs to the attack, not to the Equipment: the creature that attacked is the
        // one boosted so unattaching or moving the Equipment or sacrificing
        // the attacker doesn't stop the reveal, and the boost stays with the original attacker.
        Permanent equippedCreature = entry.getTriggeringPermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());

        List<Card> deck = gameData.playerDecks.get(defenderId);
        if (deck.isEmpty()) {
            String logEntry = defenderName + "'s library is empty — " + sourceName + "'s ability does nothing.";
            gameLogService.append(gameData, GameLog.text(logEntry));
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
        gameLogService.append(gameData, GameLog.text(revealLog));

        // Boost the equipped creature
        int revealedCount = revealedCards.size();
        int powerBoost = revealedCount * e.powerBoostPerCard();
        int toughnessBoost = revealedCount * e.toughnessBoostPerCard();
        if (equippedCreature != null && (powerBoost != 0 || toughnessBoost != 0)) {
            equippedCreature.setPowerModifier(equippedCreature.getPowerModifier() + powerBoost);
            equippedCreature.setToughnessModifier(equippedCreature.getToughnessModifier() + toughnessBoost);

            
            gameLogService.append(gameData, GameLog.builder().card(equippedCreature.getCard()).text(" gets +" + powerBoost + "/+" + toughnessBoost + " until end of turn (" + revealedCount + " " + (revealedCount != 1 ? "cards" : "card") + " revealed).").build());
        }

        // Put all revealed cards into the graveyard
        for (Card card : revealedCards) {
            graveyardService.addCardToGraveyard(gameData, defenderId, card, Zone.LIBRARY);
        }

        String millLog = defenderName + " puts " + revealedCount + " revealed "
                + (revealedCount != 1 ? "cards" : "card") + " into their graveyard.";
        gameLogService.append(gameData, GameLog.text(millLog));

        log.info("Game {} - {} reveals {} cards from {}'s library, {} gets +{}/+{}",
                gameData.id, sourceName, revealedCount, defenderName,
                equippedCreature == null ? "no creature" : equippedCreature.getCard().getName(),
                powerBoost, toughnessBoost);
    }
}
