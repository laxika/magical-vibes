package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeAndPutCountersOnEnteringCreatureEqualToPowerEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Madame Null's optional payment using the entering creature's power at resolution. */
@Component
@RequiredArgsConstructor
public class MayPayLifeAndPutCountersOnEnteringCreatureEqualToPowerEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final MayPayLifeEffectResolutionHandler mayPayLifeEffectResolutionHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayPayLifeAndPutCountersOnEnteringCreatureEqualToPowerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        // "Its power" is the stack entry's triggering permanent's, as the ability resolves (CR 608.2h).
        Permanent enteringPermanent = entry.getTriggeringPermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());
        int power = enteringPermanent == null
                ? Math.max(0, entry.getTriggeringPermanentPowerAtTrigger() == null
                        ? 0 : entry.getTriggeringPermanentPowerAtTrigger())
                : Math.max(0, gameQueryService.getEffectivePower(gameData, enteringPermanent));
        MayPayLifeEffect mayPayLife = new MayPayLifeEffect(
                power,
                PutCounterOnTargetPermanentEffect.onTriggeringPermanent(CounterType.PLUS_ONE_PLUS_ONE, power),
                "Pay " + power + " life to put " + power + " +1/+1 counter(s) on it?");
        mayPayLifeEffectResolutionHandler.resolve(gameData, entry, mayPayLife);
    }
}
