package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DestroyDamagedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOW", collectorNumber = "43")
@CardRegistration(set = "DBL", collectorNumber = "310")
public class VampireSlayer extends Card {

    public VampireSlayer() {
        // Whenever this creature deals damage to a Vampire, destroy that creature.
        addTrigger(Triggers.creatureYouControlDealsDamageToCreature(),
                new DestroyDamagedCreatureEffect(new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE)));
    }
}
