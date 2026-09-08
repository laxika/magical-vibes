package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnChosenOwnPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnDragonOfEachColorEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameIfEventValueAtLeastEffect;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class PutCountersOnDragonOfEachColorEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCountersOnDragonOfEachColorEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<CardEffect> effects = new ArrayList<>();
        for (CardColor color : CardColor.values()) {
            effects.add(new PutCounterOnChosenOwnPermanentEffect(
                    CounterType.PLUS_ONE_PLUS_ONE,
                    1,
                    new PermanentAllOfPredicate(List.of(
                            new PermanentIsCreaturePredicate(),
                            new PermanentHasSubtypePredicate(CardSubtype.DRAGON),
                            new PermanentColorInPredicate(Set.of(color)))),
                    true));
        }
        effects.add(new WinGameIfEventValueAtLeastEffect(CardColor.values().length));

        entry.setEventValue(0);
        int effectIndex = entry.getEffectsToResolve().indexOf(effect);
        if (effectIndex >= 0) {
            entry.insertEffectsToResolve(effectIndex + 1, effects);
        }
    }
}
