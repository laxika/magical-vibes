package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessDiscardsEffect;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves "counter target spell or ability unless its controller discards a card" (Ward—Discard).
 * If the controller has no cards to discard, the spell/ability is countered immediately; otherwise
 * they are prompted to discard a card or let it be countered.
 */
@Component
@RequiredArgsConstructor
public class CounterUnlessDiscardsEffectHandler implements NormalEffectHandlerBean {

    private final CounterSupport counterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterUnlessDiscardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) return;

        StackEntry targetEntry = counterSupport.findCounterTarget(gameData, targetCardId, entry);
        if (targetEntry == null) return;

        UUID targetControllerId = targetEntry.getControllerId();
        List<Card> hand = gameData.playerHands.get(targetControllerId);
        boolean hasCards = hand != null && !hand.isEmpty();

        if (!hasCards) {
            counterSupport.counterSpell(gameData, entry, targetEntry);
            return;
        }

        String prompt = "Discard a card to prevent " + targetEntry.getCard().getName() + " from being countered?";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), targetControllerId,
                List.of(new CounterUnlessDiscardsEffect()),
                prompt, targetCardId));
    }
}
