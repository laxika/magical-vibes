package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessDiscardsEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessDiscardsOrPaysEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves a counter-unless ransom that offers either a discard or a generic mana payment.
 */
@Component
@RequiredArgsConstructor
public class CounterUnlessDiscardsOrPaysEffectHandler implements NormalEffectHandlerBean {

    private final CounterSupport counterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterUnlessDiscardsOrPaysEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CounterUnlessDiscardsOrPaysEffect ransom = (CounterUnlessDiscardsOrPaysEffect) effect;
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) return;

        StackEntry targetEntry = counterSupport.findCounterTarget(gameData, targetCardId, entry);
        if (targetEntry == null) return;

        UUID targetControllerId = targetEntry.getControllerId();
        List<Card> hand = gameData.playerHands.get(targetControllerId);
        boolean hasCards = hand != null && !hand.isEmpty();
        ManaPool pool = gameData.playerManaPools.get(targetControllerId);
        boolean canPay = new ManaCost("{" + ransom.amount() + "}").canPay(pool);

        if (!hasCards && !canPay) {
            counterSupport.counterSpell(gameData, entry, targetEntry);
            return;
        }

        String prompt = "Pay {" + ransom.amount() + "} to prevent "
                + targetEntry.getCard().getName() + " from being countered?";
        if (hasCards && canPay) {
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(), targetControllerId,
                    List.of(ransom), prompt, targetCardId, entry.getControllerId()));
        } else if (hasCards) {
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(), targetControllerId,
                    List.of(new CounterUnlessDiscardsEffect()),
                    "Discard a card to prevent " + targetEntry.getCard().getName()
                            + " from being countered?",
                    targetCardId, entry.getControllerId()));
        } else {
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(), targetControllerId,
                    List.of(new CounterUnlessPaysEffect(ransom.amount())), prompt,
                    targetCardId, entry.getControllerId()));
        }
    }
}
