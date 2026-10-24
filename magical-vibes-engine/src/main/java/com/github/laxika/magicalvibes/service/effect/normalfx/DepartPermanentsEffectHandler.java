package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DepartureBatch;
import com.github.laxika.magicalvibes.model.DepartureCause;
import com.github.laxika.magicalvibes.model.DepartureIntent;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DepartPermanentsEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Departs the permanents an earlier step or choice chose as this step ({@link StepDepartureSupport#depart}), all as
 * one event. A permanent that has left in the meantime is skipped, and so is one that can't be sacrificed any more
 * (CR 701.21a).
 */
@Component
@RequiredArgsConstructor
public class DepartPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final StepDepartureSupport stepDepartureSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DepartPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<DepartureIntent> intents = ((DepartPermanentsEffect) effect).intents().stream()
                .filter(intent -> canDepart(gameData, intent))
                .toList();
        if (intents.isEmpty()) {
            entry.recordDepartures(List.of());
            return;
        }
        stepDepartureSupport.depart(gameData, entry, effect, DepartureBatch.of(intents));
    }

    private boolean canDepart(GameData gameData, DepartureIntent intent) {
        Permanent permanent = gameQueryService.findPermanentById(gameData, intent.permanentId());
        return permanent != null
                && (intent.cause() != DepartureCause.SACRIFICE || !gameQueryService.cantBeSacrificed(gameData, permanent));
    }
}
