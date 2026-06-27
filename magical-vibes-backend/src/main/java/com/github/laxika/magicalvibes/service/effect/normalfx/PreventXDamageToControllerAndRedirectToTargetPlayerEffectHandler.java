package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DamageRedirectShield;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventXDamageToControllerAndRedirectToTargetPlayerEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PreventXDamageToControllerAndRedirectToTargetPlayerEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventXDamageToControllerAndRedirectToTargetPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID targetPlayerId = entry.getTargetId();
        int xValue = entry.getXValue();

        if (xValue <= 0 || targetPlayerId == null) return;

        gameData.damageRedirectShields.add(new DamageRedirectShield(
                controllerId, xValue, entry.getSourcePermanentId(), entry.getCard(), targetPlayerId));

        String controllerName = gameData.playerIdToName.get(controllerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String logEntry = entry.getCard().getName() + " — the next " + xValue + " damage that would be dealt to "
                + controllerName + " this turn is prevented. If prevented, " + entry.getCard().getName()
                + " deals that much damage to " + targetName + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - Damage redirect shield {} added: protecting {} → redirecting to {}",
                gameData.id, xValue, controllerName, targetName);
    }
}
