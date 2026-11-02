package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DestroyDamagedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RIX", collectorNumber = "67")
public class DinosaurHunter extends Card {

    public DinosaurHunter() {
        addTrigger(Triggers.creatureYouControlDealsDamageToCreature(),
                new DestroyDamagedCreatureEffect(new PermanentHasSubtypePredicate(CardSubtype.DINOSAUR)));
    }
}
