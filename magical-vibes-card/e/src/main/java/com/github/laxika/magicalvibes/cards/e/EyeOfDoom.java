package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesNonlandPermanentAndPutCounterEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "C13", collectorNumber = "243")
public class EyeOfDoom extends Card {

    public EyeOfDoom() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
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
