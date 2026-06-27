package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageIfFewCardsInHandEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import com.github.laxika.magicalvibes.model.Card;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealDamageIfFewCardsInHandEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageIfFewCardsInHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageIfFewCardsInHandEffect) effect;

        UUID targetId = entry.getTargetId();
        String cardName = entry.getCard().getName();

        if (!gameData.playerIds.contains(targetId)) return;

        // Intervening-if: re-check condition at resolution time
        List<Card> hand = gameData.playerHands.get(targetId);
        int handSize = hand != null ? hand.size() : 0;
        if (handSize > e.maxCards()) {
            String playerName = gameData.playerIdToName.get(targetId);
            gameBroadcastService.logAndBroadcast(gameData,
                    cardName + "'s ability does nothing — " + playerName + " has more than " + e.maxCards() + " cards in hand.");
            return;
        }

        if (!damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) {
            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, e.damage(), entry);
            damageSupport.dealDamageToPlayer(gameData, entry, targetId, rawDamage);
        }

        gameOutcomeService.checkWinCondition(gameData);
    
    }
}
