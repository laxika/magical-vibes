package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "INR", collectorNumber = "43")
public class SubjugatorAngel extends Card {

    public SubjugatorAngel() {
        // When this creature enters, tap all creatures your opponents control.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new TapPermanentsEffect(
                TapUntapScope.ALL_CREATURES,
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
    }
}
