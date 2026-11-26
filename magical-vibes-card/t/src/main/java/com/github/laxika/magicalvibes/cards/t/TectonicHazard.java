package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "LCI", collectorNumber = "169")
public class TectonicHazard extends Card {

    public TectonicHazard() {
        addSimultaneousSpellEffects(DealDamageEffect.to(Subjects.eachOpponent(), 1),
                DealDamageEffect.toEachCreature(1,
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
    }
}
