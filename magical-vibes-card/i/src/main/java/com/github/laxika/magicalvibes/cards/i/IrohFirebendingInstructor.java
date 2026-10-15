package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "TLE", collectorNumber = "240")
@CardRegistration(set = "TLE", collectorNumber = "282")
public class IrohFirebendingInstructor extends Card {

    public IrohFirebendingInstructor() {
        addEffect(EffectSlot.ON_ATTACK, new BoostAllOwnCreaturesEffect(
                1, 1, new PermanentIsAttackingPredicate()));
    }
}
