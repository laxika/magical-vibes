package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ODY", collectorNumber = "39")
@CardRegistration(set = "VMA", collectorNumber = "39")
@CardRegistration(set = "CMM", collectorNumber = "50")
public class PiannaNomadCaptain extends Card {

    public PiannaNomadCaptain() {
        // Whenever Pianna attacks, attacking creatures get +1/+1 until end of turn.
        addTrigger(Triggers.attacks(),
                new BoostAllCreaturesEffect(1, 1, new PermanentIsAttackingPredicate()));
    }
}
