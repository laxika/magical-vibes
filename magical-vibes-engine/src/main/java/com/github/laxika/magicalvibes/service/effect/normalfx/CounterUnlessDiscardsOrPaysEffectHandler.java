package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessDiscardsEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessDiscardsOrPaysEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.service.effect.cost.ResolutionCostPaymentService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves a counter-unless ransom that offers either a discard or a generic mana payment. The mana can be paid when
 * the payer's pool or the mana they could still make covers it (CR 118.3; they may activate mana abilities while
 * asked, CR 605.3a), so the question carries the mana cost.
 */
@Component
@RequiredArgsConstructor
public class CounterUnlessDiscardsOrPaysEffectHandler implements NormalEffectHandlerBean {

    private final CounterSupport counterSupport;
    private final ResolutionCostPaymentService resolutionCostPaymentService;

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
        String manaCost = "{" + ransom.amount() + "}";
        boolean canPay = resolutionCostPaymentService.canPayMana(gameData, targetControllerId, new ManaCost(manaCost));

        if (!hasCards && !canPay) {
            counterSupport.counterSpell(gameData, entry, targetEntry);
            return;
        }

        String prompt = "Pay " + manaCost + " to prevent "
                + targetEntry.getCard().getName() + " from being countered?";
        if (hasCards && canPay) {
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(), targetControllerId, List.of(ransom), prompt, targetCardId, manaCost,
                    entry.getSourcePermanentId(), null, 0, 0, null, null, null, entry.getSourcePermanentSnapshot(),
                    entry.getControllerId()));
        } else if (hasCards) {
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(), targetControllerId,
                    List.of(new CounterUnlessDiscardsEffect()),
                    "Discard a card to prevent " + targetEntry.getCard().getName()
                            + " from being countered?",
                    targetCardId, entry.getControllerId()));
        } else {
            // With no card to discard, only the mana can be paid: the next step asks for it.
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1,
                    List.of(UnlessPaysEffect.counterUnlessPays(ransom.amount())));
        }
    }
}
