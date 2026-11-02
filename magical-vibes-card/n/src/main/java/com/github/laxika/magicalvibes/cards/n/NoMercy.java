package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyDamageSourcePermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ULG", collectorNumber = "56")
@CardRegistration(set = "MP2", collectorNumber = "43")
@CardRegistration(set = "DMR", collectorNumber = "95")
@CardRegistration(set = "MAR", collectorNumber = "68")
public class NoMercy extends Card {

    public NoMercy() {
        // Whenever a creature deals damage to you, destroy it.
        addTrigger(Triggers.permanentDealsDamageToYou(),
                new DestroyDamageSourcePermanentEffect(new PermanentIsCreaturePredicate()));
    }
}
