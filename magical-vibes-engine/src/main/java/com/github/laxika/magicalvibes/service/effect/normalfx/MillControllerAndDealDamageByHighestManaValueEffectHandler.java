package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndDealDamageByHighestManaValueEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import com.github.laxika.magicalvibes.model.Card;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MillControllerAndDealDamageByHighestManaValueEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
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
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            return;
        }

        // Snapshot the cards before milling to compute highest mana value
        List<Card> milledCards = new ArrayList<>(deck.subList(0, cardsToMill));

        int highestManaValue = milledCards.stream()
                .mapToInt(Card::getManaValue)
                .max()
                .orElse(0);

        // Perform the actual mill
        graveyardService.resolveMillPlayer(gameData, controllerId, e.count());

        if (highestManaValue == 0) {
            String logEntry = cardName + " deals 0 damage (greatest mana value among milled cards is 0).";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            return;
        }

        int damage = gameQueryService.applyDamageMultiplier(gameData, highestManaValue, entry);
        damageSupport.resolveAnyTargetDamage(gameData, entry, targetId, damage, false);
        gameOutcomeService.checkWinCondition(gameData);
    
    }
}
