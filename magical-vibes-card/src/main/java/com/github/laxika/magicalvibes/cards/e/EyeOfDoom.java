package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesNonlandPermanentAndPutCounterEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;

import java.util.List;

@CardRegistration(set = "C13", collectorNumber = "243")
public class EyeOfDoom extends Card {

    public EyeOfDoom() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EachPlayerChoosesNonlandPermanentAndPutCounterEffect(CounterType.DOOM));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new SacrificeSelfCost(),
                        new DestroyAllPermanentsEffect(new PermanentHasCountersPredicate(CounterType.DOOM))
                ),
                "{2}, {T}, Sacrifice this artifact: Destroy each permanent with a doom counter on it."
        ));
    }
}
