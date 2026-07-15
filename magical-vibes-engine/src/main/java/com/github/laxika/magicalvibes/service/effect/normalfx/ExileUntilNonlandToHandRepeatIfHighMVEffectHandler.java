package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileUntilNonlandToHandRepeatIfHighMVEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import java.util.List;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileUntilNonlandToHandRepeatIfHighMVEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileUntilNonlandToHandRepeatIfHighMVEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileUntilNonlandToHandRepeatIfHighMVEffect) effect;

        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        int cardsToHand = 0;
        boolean repeat = true;

        while (repeat) {
            repeat = false;

            if (deck.isEmpty()) {
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
                break;
            }

            // Exile cards until a nonland card is found
            boolean foundNonland = false;
            while (!deck.isEmpty()) {
                Card card = deck.removeFirst();

                if (card.hasType(CardType.LAND)) {
                    gameData.addToExile(controllerId, card);
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " exiles " + card.getName() + " (land) (" + sourceName + ")."));
                } else {
                    // Nonland card — put into hand
                    gameData.addCardToHand(controllerId, card);
                    cardsToHand++;
                    int manaValue = card.getManaValue();
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " exiles " + card.getName() + " (mana value " + manaValue
                                    + ") and puts it into their hand (" + sourceName + ")."));

                    if (manaValue >= e.manaValueThreshold()) {
                        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(card.getName() + " has mana value " + manaValue + " or greater — repeating the process."));
                        repeat = true;
                    }
                    foundNonland = true;
                    break;
                }
            }

            if (!foundNonland && deck.isEmpty()) {
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + "'s library is empty — no nonland card found (" + sourceName + ")."));
            }
        }

        // Deal damage to controller for each card put into hand (all at once per ruling)
        if (cardsToHand > 0) {
            int totalDamage = cardsToHand * e.damagePerCard();
            if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(sourceName + "'s damage to " + playerName + " is prevented."));
            } else {
                int rawDamage = gameQueryService.applyDamageMultiplier(gameData, totalDamage, entry);
                damageSupport.dealDamageToPlayer(gameData, entry, controllerId, rawDamage);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(sourceName + " deals " + rawDamage + " damage to " + playerName
                                + " (" + cardsToHand + " card" + (cardsToHand > 1 ? "s" : "") + " put into hand)."));
            }

            gameOutcomeService.checkWinCondition(gameData);
        }

        log.info("Game {} - {} resolved {} ETB: {} cards to hand",
                gameData.id, playerName, sourceName, cardsToHand);
    
    }
}
