package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CreatureDamageRedirectShield;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectNextDamageToTargetCreatureToControllerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedirectNextDamageToTargetCreatureToControllerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RedirectNextDamageToTargetCreatureToControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RedirectNextDamageToTargetCreatureToControllerEffect) effect;
        UUID protectedPermanentId = entry.getTargetId();
        UUID controllerId = entry.getControllerId();
        if (protectedPermanentId == null || controllerId == null) return;

        Permanent protectedPerm = gameQueryService.findPermanentById(gameData, protectedPermanentId);
        if (protectedPerm == null) return;

        int amount = amountEvaluationService.evaluate(gameData, e.amount(),
                AmountContext.forStackEntry(entry, protectedPerm));
        if (amount <= 0) return;

        gameData.creatureDamageRedirectShields.add(new CreatureDamageRedirectShield(
                protectedPermanentId, null, amount, controllerId));

        String controllerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.builder()
                .text("The next " + amount + " damage that would be dealt this turn to ")
                .card(protectedPerm.getCard())
                .text(" is dealt to " + controllerName + " instead.")
                .build());
        log.info("Game {} - registered next-{}-damage redirect from {} to controller {}", gameData.id,
                amount, protectedPerm.getCard().getName(), controllerName);
    }
}
