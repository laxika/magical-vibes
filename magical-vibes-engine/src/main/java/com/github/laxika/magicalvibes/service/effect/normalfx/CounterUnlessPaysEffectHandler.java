package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
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
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterUnlessPaysEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CounterUnlessPaysEffect) effect;
        List<UUID> boundTargets = entry.targetsForBoundEffectGroup(effect);
        UUID targetCardId = boundTargets == null
                ? entry.getTargetId()
                : boundTargets.stream().findFirst().orElse(null);
        if (targetCardId == null) return;

        StackEntry targetEntry = counterSupport.findCounterTarget(gameData, targetCardId, entry);
        if (targetEntry == null) return;

        int payAmount;
        if (e.dynamicAmount() != null) {
            // Source-relative amounts use the live source permanent when it is still on the
            // battlefield, else the last-known snapshot (e.g. sacrificed as an activation cost).
            Permanent source = entry.getSourcePermanentId() != null
                    ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                    : null;
            if (source == null) {
                source = entry.getSourcePermanentSnapshot();
            }
            payAmount = amountEvaluationService.evaluate(gameData, e.dynamicAmount(),
                    AmountContext.forStackEntry(entry, source));
        } else {
            payAmount = e.useXValue() ? entry.getXValue() : e.amount();
        }
        UUID targetControllerId = targetEntry.getControllerId();
        ManaPool pool = gameData.playerManaPools.get(targetControllerId);
        String manaCost = e.manaCost() != null ? e.manaCost() : "{" + payAmount + "}";
        ManaCost cost = new ManaCost(manaCost);
        int lifeCost = e.lifeCost();
        boolean canPayLife = lifeCost <= 0
                || (gameQueryService.canPlayerLifeChange(gameData, targetControllerId)
                        && gameData.getLife(targetControllerId) >= lifeCost);

        if (!cost.canPay(pool) || !canPayLife) {
            if (e.exileIfCountered()) {
                counterSupport.counterSpellAndExile(gameData, entry, targetEntry);
            } else {
                counterSupport.counterSpell(gameData, entry, targetEntry);
            }
            // Not paid (couldn't afford): resolve any rider against the spell's controller (Power Sink).
            counterSupport.resolveNotPaidRider(gameData, entry.getCard(), targetControllerId, e.onNotPaidEffects());
        } else {
            String costText = manaCost.equals("{0}") && lifeCost > 0
                    ? lifeCost + " life"
                    : manaCost + (lifeCost > 0 ? " and " + lifeCost + " life" : "");
            String prompt = "Pay " + costText + " to prevent "
                    + targetEntry.getCard().getName() + " from being countered?";
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(), targetControllerId,
                    List.of(new CounterUnlessPaysEffect(payAmount, false, e.exileIfCountered(),
                            null, e.onNotPaidEffects(), lifeCost, e.manaCost(), e.onPaidEffects())),
                    prompt, targetCardId, entry.getControllerId()
            ));
        }
    }
}
