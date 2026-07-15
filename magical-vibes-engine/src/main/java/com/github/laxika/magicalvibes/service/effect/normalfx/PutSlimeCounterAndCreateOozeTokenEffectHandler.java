package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.CountersOnLinkedPermanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutSlimeCounterAndCreateOozeTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutSlimeCounterAndCreateOozeTokenEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutSlimeCounterAndCreateOozeTokenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        
                UUID sourcePermId = entry.getSourcePermanentId();
                if (sourcePermId == null) {
                    log.warn("Game {} - PutSlimeCounterAndCreateOozeTokenEffect has no sourcePermanentId", gameData.id);
                    return;
                }

                Permanent source = gameQueryService.findPermanentById(gameData, sourcePermId);
                if (source == null) {
                    log.info("Game {} - Gutter Grime no longer on battlefield, effect fizzles", gameData.id);
                    return;
                }

                if (gameQueryService.cantHaveCounters(gameData, source)) {
                    return;
                }

                // Put a slime counter on the source
                source.setCounterCount(CounterType.SLIME, source.getCounterCount(CounterType.SLIME) + 1);
                int slimeCount = source.getCounterCount(CounterType.SLIME);

                String counterLog = source.getCard().getName() + " gets a slime counter (" + slimeCount + " total).";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(counterLog));
                log.info("Game {} - {} gets a slime counter ({} total)", gameData.id, source.getCard().getName(), slimeCount);

                // Create a 0/0 green Ooze token with a CDA linking to this Gutter Grime
                CountersOnLinkedPermanent slimeOnGrime = new CountersOnLinkedPermanent(CounterType.SLIME, sourcePermId);
                CreateTokenEffect tokenEffect = new CreateTokenEffect(
                        1, "Ooze", 0, 0,
                        CardColor.GREEN, List.of(CardSubtype.OOZE),
                        Set.of(), Set.of(),
                        Map.of(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(slimeOnGrime, slimeOnGrime))
                );
                permanentControlSupport.applyCreateToken(gameData, entry.getControllerId(), tokenEffect, entry.getCard().getSetCode());
    
    }
}
