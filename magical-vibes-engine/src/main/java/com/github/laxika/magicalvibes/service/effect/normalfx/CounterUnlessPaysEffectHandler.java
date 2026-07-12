package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CounterUnlessPaysEffectHandler implements NormalEffectHandlerBean {

    private final CounterSupport counterSupport;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterUnlessPaysEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CounterUnlessPaysEffect) effect;
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) return;

        StackEntry targetEntry = counterSupport.findCounterTarget(gameData, targetCardId, entry);
        if (targetEntry == null) return;

        int payAmount;
        if (e.dynamicAmount() != null) {
            payAmount = amountEvaluationService.evaluate(gameData, e.dynamicAmount(),
                    AmountContext.forStackEntry(entry, null));
        } else {
            payAmount = e.useXValue() ? entry.getXValue() : e.amount();
        }
        UUID targetControllerId = targetEntry.getControllerId();
        ManaPool pool = gameData.playerManaPools.get(targetControllerId);
        ManaCost cost = new ManaCost("{" + payAmount + "}");

        if (!cost.canPay(pool)) {
            if (e.exileIfCountered()) {
                counterSupport.counterSpellAndExile(gameData, entry, targetEntry);
            } else {
                counterSupport.counterSpell(gameData, entry, targetEntry);
            }
        } else {
            String prompt = "Pay {" + payAmount + "} to prevent " + targetEntry.getCard().getName() + " from being countered?";
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(), targetControllerId,
                    List.of(new CounterUnlessPaysEffect(payAmount, false, e.exileIfCountered())),
                    prompt, targetCardId
            ));
        }
    }
}
