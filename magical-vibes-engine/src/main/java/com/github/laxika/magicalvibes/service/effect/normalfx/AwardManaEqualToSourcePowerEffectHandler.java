package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AwardManaEqualToSourcePowerEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link AwardManaEqualToSourcePowerEffect} as part of a spell or ability that uses
 * the stack (e.g. Molten-Core Maestro's Opus trigger), mirroring the mana-ability path in
 * {@code ActivatedAbilityExecutionService}. The source permanent is resolved from the stack
 * entry's {@code sourcePermanentId} and mana equal to its effective power is added to the
 * controller's pool.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AwardManaEqualToSourcePowerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AwardManaEqualToSourcePowerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (AwardManaEqualToSourcePowerEffect) effect;
        UUID sourceId = entry.getSourcePermanentId();
        if (sourceId == null) {
            return;
        }
        Permanent source = gameQueryService.findPermanentById(gameData, sourceId);
        if (source == null) {
            return;
        }

        int power = gameQueryService.getEffectivePower(gameData, source);
        if (power <= 0) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        ManaPool pool = gameData.playerManaPools.get(controllerId);
        pool.add(e.color(), power);
        if (gameQueryService.isCreature(gameData, source)) {
            pool.addCreatureMana(e.color(), power);
        }

        String playerName = gameData.playerIdToName.get(controllerId);
        String logEntry = playerName + " adds " + power + " " + e.color().getCode()
                + " from " + source.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} adds {} {} from {} (power)", gameData.id, playerName, power, e.color(),
                source.getCard().getName());
    }
}
