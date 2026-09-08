package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CycloneUpkeepEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Cyclone's wind-counter upkeep trigger and creates its pay-or-sacrifice prompt. */
@Component
@RequiredArgsConstructor
public class CycloneUpkeepEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CycloneUpkeepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        permanentCounterSupport.placeCounterOnPermanent(gameData, entry, source, CounterType.WIND, 1);
        int windCounters = source.getCounterCount(CounterType.WIND);
        if (windCounters <= 0) {
            return;
        }

        String manaCost = "{G}".repeat(windCounters);
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                entry.getControllerId(),
                List.of(effect),
                entry.getCard().getName() + " - Pay " + manaCost + "?",
                null,
                manaCost,
                entry.getSourcePermanentId()));
    }
}
