package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureUnlessControllerTakesDamageEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a targeted creature's damage-or-controller-damage choice. */
@Component
@RequiredArgsConstructor
public class DealDamageToTargetCreatureUnlessControllerTakesDamageEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToTargetCreatureUnlessControllerTakesDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToTargetCreatureUnlessControllerTakesDamageEffect) effect;
        UUID targetCreatureId = entry.getTargetId();
        Permanent target = gameQueryService.findPermanentById(gameData, targetCreatureId);
        if (target == null) {
            return;
        }

        UUID targetControllerId = gameQueryService.findPermanentController(gameData, targetCreatureId);
        if (targetControllerId == null) {
            return;
        }

        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        AmountContext amountContext = AmountContext.forStackEntry(entry, source);
        int controllerDamage = amountEvaluationService.evaluate(gameData, e.controllerDamage(), amountContext);
        int targetDamage = amountEvaluationService.evaluate(gameData, e.targetDamage(), amountContext);
        String prompt = "Have " + entry.getCard().getName() + " deal " + controllerDamage
                + " damage to you? If you don't, " + entry.getCard().getName() + " deals "
                + targetDamage + " damage to " + target.getCard().getName() + ". ("
                + entry.getCard().getName() + ")";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), targetControllerId, List.of(e), prompt,
                targetCreatureId, null, entry.getSourcePermanentId(), null, 0, 0,
                null, null, null, entry.getSourcePermanentSnapshot(), entry.getControllerId(), null, 0));
    }
}
