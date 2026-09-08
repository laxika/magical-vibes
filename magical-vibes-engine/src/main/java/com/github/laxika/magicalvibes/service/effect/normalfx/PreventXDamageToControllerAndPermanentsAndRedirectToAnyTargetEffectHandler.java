package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DamageRedirectShield;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventXDamageToControllerAndPermanentsAndRedirectToAnyTargetEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Divine Deflection: adds a {@link DamageRedirectShield} for X that covers the controller and the
 * permanents they control; each point it prevents is dealt by this card to the chosen target.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PreventXDamageToControllerAndPermanentsAndRedirectToAnyTargetEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventXDamageToControllerAndPermanentsAndRedirectToAnyTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID targetId = entry.getTargetId();
        var redirectEffect = (PreventXDamageToControllerAndPermanentsAndRedirectToAnyTargetEffect) effect;
        int amount = amountEvaluationService.evaluate(gameData, redirectEffect.amount(),
                AmountContext.forStackEntry(entry, null));

        if (amount <= 0 || targetId == null) return;

        gameData.damageRedirectShields.add(new DamageRedirectShield(
                controllerId, amount, entry.getSourcePermanentId(), entry.getCard(), targetId, true));

        String controllerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" — the next " + amount + " damage that would be dealt to " + controllerName
                        + " and/or the permanents they control this turn is prevented.")
                .build());
        log.info("Game {} - Divine Deflection shield {} added: protecting {} and their permanents",
                gameData.id, amount, controllerName);
    }
}
