package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "KTK", collectorNumber = "34")
@CardRegistration(set = "SLD", collectorNumber = "1429")
@CardRegistration(set = "EA2", collectorNumber = "9")
@CardRegistration(set = "MAR", collectorNumber = "8")
@CardRegistration(set = "OMB", collectorNumber = "8")
public class CleverImpersonator extends Card {

    public CleverImpersonator() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CopyPermanentOnEnterEffect(
                new PermanentNotPredicate(new PermanentIsLandPredicate()), "nonland permanent"
        ));
    }
}
