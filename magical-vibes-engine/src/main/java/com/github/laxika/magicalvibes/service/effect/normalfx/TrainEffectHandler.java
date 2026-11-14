package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.rules.CounterChangeEvent;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TrainEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Training (CR 702.149a): puts a +1/+1 counter on the creature, which then "trains"; each of its "whenever this
 * creature trains" abilities triggers and goes on the stack through the stack door, which makes its choices as it goes
 * there (Savior of Ollenbock's "up to one other target creature or creature card from a graveyard").
 */
@Component
@RequiredArgsConstructor
public class TrainEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TrainEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        int placed = permanentCounterSupport.placeCounterOnPermanent(
                gameData, entry, source, CounterType.PLUS_ONE_PLUS_ONE, 1);
        if (placed <= 0) {
            return;
        }

        gameData.rulesEvents.occur(CounterChangeEvent.of(CounterChangeEvent.Kind.TRAINED, source,
                entry.getControllerId(), null, 1));
    }
}
