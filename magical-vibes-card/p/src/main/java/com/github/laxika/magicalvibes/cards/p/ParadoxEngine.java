package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AER", collectorNumber = "169")
@CardRegistration(set = "MPS", collectorNumber = "43")
@CardRegistration(set = "KLR", collectorNumber = "259")
public class ParadoxEngine extends Card {

    public ParadoxEngine() {
        addTrigger(Triggers.youCastSpell().anySpell(),
                new UntapPermanentsEffect(
                        TapUntapScope.CONTROLLED,
                        new PermanentNotPredicate(new PermanentIsLandPredicate())));
    }
}
