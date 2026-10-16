package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndDealDamageByHighestManaValueEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.UUID;
import java.util.List;
import com.github.laxika.magicalvibes.model.Card;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MillControllerAndDealDamageByHighestManaValueEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillControllerAndDealDamageByHighestManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (MillControllerAndDealDamageByHighestManaValueEffect) effect;

        UUID targetId = entry.getTargetId();
        if (targetId == null) return;

        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String cardName = entry.getCard().getName();
        String controllerName = gameData.playerIdToName.get(controllerId);

        int cardsToMill = Math.min(e.count(), deck.size());

        if (cardsToMill == 0) {
            String logEntry = controllerName + "'s library is empty — " + cardName + " deals no damage.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            return;
        }

        // Replacement effects (e.g. an opponent's mill doubler) can mill more cards than the count,
        // and every card milled in the event counts toward the greatest mana value.
        List<Card> milledCards = graveyardService.resolveMillPlayerAndReturnAllMilledCards(
                gameData, controllerId, e.count());

        int highestManaValue = milledCards.stream()
                .mapToInt(Card::getManaValue)
                .max()
                .orElse(0);

        if (highestManaValue == 0) {
            String logEntry = cardName + " deals 0 damage (greatest mana value among milled cards is 0).";
            gameLogService.append(gameData, GameLog.text(logEntry));
            return;
        }

        int damage = highestManaValue;
        damageSupport.resolveAnyTargetDamage(gameData, entry, targetId, damage, false);
        gameOutcomeService.checkWinCondition(gameData);
    
    }
}
