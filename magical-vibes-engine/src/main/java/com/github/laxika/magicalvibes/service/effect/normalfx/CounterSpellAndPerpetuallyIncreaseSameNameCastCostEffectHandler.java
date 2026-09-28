package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellAndPerpetuallyIncreaseSameNameCastCostEffect;
import com.github.laxika.magicalvibes.service.cast.PerpetualCardCastCostSupport;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Teferi's Contingency's counter and same-name perpetual tax. */
@Component
@RequiredArgsConstructor
public class CounterSpellAndPerpetuallyIncreaseSameNameCastCostEffectHandler
        implements NormalEffectHandlerBean {

    private final CounterSupport counterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterSpellAndPerpetuallyIncreaseSameNameCastCostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) {
            return;
        }

        StackEntry targetEntry = gameData.stack.stream()
                .filter(stackEntry -> stackEntry.getTargetableId().equals(targetCardId))
                .findFirst()
                .orElse(null);
        if (targetEntry == null || targetEntry.getCard() == null) {
            return;
        }

        String spellName = targetEntry.getCard().getName();
        UUID spellControllerId = targetEntry.getControllerId();
        StackEntry counterableTarget = counterSupport.findCounterTarget(gameData, targetCardId, entry);
        if (counterableTarget != null) {
            counterSupport.counterSpell(gameData, entry, counterableTarget);
        }

        CounterSpellAndPerpetuallyIncreaseSameNameCastCostEffect taxEffect =
                (CounterSpellAndPerpetuallyIncreaseSameNameCastCostEffect) effect;
        rememberMatchingCards(gameData, spellControllerId, spellName, taxEffect.amount());
    }

    private void rememberMatchingCards(GameData gameData, UUID playerId, String name, int amount) {
        rememberMatchingCards(gameData, gameData.playerGraveyards.get(playerId), name, amount);
        rememberMatchingCards(gameData, gameData.playerHands.get(playerId), name, amount);
        rememberMatchingCards(gameData, gameData.playerDecks.get(playerId), name, amount);
    }

    private void rememberMatchingCards(GameData gameData, List<Card> cards, String name, int amount) {
        if (cards == null) {
            return;
        }
        for (Card card : cards) {
            if (name.equals(card.getName())) {
                PerpetualCardCastCostSupport.rememberIncrease(gameData, card, amount);
            }
        }
    }
}
