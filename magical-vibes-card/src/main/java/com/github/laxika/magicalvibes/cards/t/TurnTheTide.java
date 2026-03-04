package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "MBS", collectorNumber = "35")
public class TurnTheTide extends Card {

    public TurnTheTide() {
        addEffect(EffectSlot.SPELL,
                new BoostAllCreaturesEffect(-2, 0,
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
    }
}
