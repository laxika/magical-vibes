package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DelayingShieldUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Delaying Shield's counter removal and repeated upkeep payment decisions. */
@Component
@RequiredArgsConstructor
public class DelayingShieldUpkeepEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DelayingShieldUpkeepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        int removed = source.getCounterCount(CounterType.DELAY);
        source.setCounterCount(CounterType.DELAY, 0);
        if (removed <= 0) {
            return;
        }

        List<CardEffect> payments = new ArrayList<>(removed);
        for (int i = 0; i < removed; i++) {
            payments.add(new ForcedCostOrElseEffect(
                    new PayManaCost("{1}{W}"),
                    List.of(new LoseLifeEffect(1, LoseLifeRecipient.CONTROLLER)),
                    true));
        }
        entry.getEffectsToResolve().addAll(payments);
    }
}
