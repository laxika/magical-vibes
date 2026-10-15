package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsForAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "DSC", collectorNumber = "35")
@CardRegistration(set = "DSC", collectorNumber = "62")
public class ShriekwoodDevourer extends Card {

    public ShriekwoodDevourer() {
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                new UntapPermanentsForAmountEffect(
                        new GreatestPowerAmongControlled(new PermanentIsAttackingPredicate()),
                        new PermanentIsLandPredicate()));
    }
}
