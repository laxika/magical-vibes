package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysLifeEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a dynamic life-payment ward cost. */
@Component
@RequiredArgsConstructor
public class CounterUnlessPaysLifeEffectHandler implements NormalEffectHandlerBean {

    private final CounterSupport counterSupport;
    private final AmountEvaluationService amountEvaluationService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterUnlessPaysLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CounterUnlessPaysLifeEffect paysLife = (CounterUnlessPaysLifeEffect) effect;
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) return;

        StackEntry targetEntry = counterSupport.findCounterTarget(gameData, targetCardId, entry);
        if (targetEntry == null) return;

        Permanent sourcePermanent = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (sourcePermanent == null) {
            sourcePermanent = entry.getSourcePermanentSnapshot();
        }
        int lifeCost = amountEvaluationService.evaluate(gameData, paysLife.amount(),
                AmountContext.forStackEntry(entry, sourcePermanent));
        UUID targetControllerId = targetEntry.getControllerId();
        boolean canPayLife = lifeCost <= 0
                || (gameQueryService.canPlayerLifeChange(gameData, targetControllerId)
                        && gameData.getLife(targetControllerId) >= lifeCost);

        if (!canPayLife) {
            counterSupport.counterSpell(gameData, entry, targetEntry);
            return;
        }

        String prompt = "Pay " + lifeCost + " life to prevent "
                + targetEntry.getCard().getName() + " from being countered?";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), targetControllerId,
                List.of(new CounterUnlessPaysEffect(0, false, false, null, List.of(), lifeCost)),
                prompt, targetCardId));
    }
}
