package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "FIN", collectorNumber = "505")
@CardRegistration(set = "FIN", collectorNumber = "553")
public class SephirothPlanetsHeir extends Card {

    public SephirothPlanetsHeir() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new BoostAllCreaturesEffect(-2, -2,
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
        addEffect(EffectSlot.ON_OPPONENT_CREATURE_DIES, new PutCountersOnSourceEffect(1, 1, 1));
    }
}
