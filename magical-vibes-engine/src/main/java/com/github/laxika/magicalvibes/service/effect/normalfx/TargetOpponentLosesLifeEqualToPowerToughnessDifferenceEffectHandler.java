package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetOpponentLosesLifeEqualToPowerToughnessDifferenceEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TargetOpponentLosesLifeEqualToPowerToughnessDifferenceEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetOpponentLosesLifeEqualToPowerToughnessDifferenceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)
                || targetPlayerId.equals(entry.getControllerId())) {
            return;
        }

        Permanent triggeringPermanent = entry.getTriggeringPermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());
        int power;
        int toughness;
        if (triggeringPermanent != null) {
            power = gameQueryService.getEffectivePower(gameData, triggeringPermanent);
            toughness = gameQueryService.getEffectiveToughness(gameData, triggeringPermanent);
        } else {
            power = entry.getTriggeringPermanentPowerAtTrigger() == null
                    ? 0 : entry.getTriggeringPermanentPowerAtTrigger();
            toughness = entry.getTriggeringPermanentToughnessAtTrigger() == null
                    ? 0 : entry.getTriggeringPermanentToughnessAtTrigger();
        }

        long difference = Math.abs((long) power - toughness);
        lifeSupport.applyLifeLoss(gameData, targetPlayerId,
                (int) Math.min(Integer.MAX_VALUE, difference), entry.getCard().getName());
    }
}
