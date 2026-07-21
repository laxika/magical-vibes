package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "ARB", collectorNumber = "85")
public class ZealousPersecution extends Card {

    public ZealousPersecution() {
        // Until end of turn, creatures you control get +1/+1.
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(1, 1));

        // Until end of turn, creatures your opponents control get -1/-1
        // (opponents' creatures = every creature not controlled by this spell's controller).
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(-1, -1,
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
    }
}
