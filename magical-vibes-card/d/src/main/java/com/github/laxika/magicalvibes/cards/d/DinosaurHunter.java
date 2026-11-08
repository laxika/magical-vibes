package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RIX", collectorNumber = "67")
public class DinosaurHunter extends Card {

    public DinosaurHunter() {
        addTrigger(Triggers.dealsDamageToCreature().withDamagedFilter(new PermanentHasSubtypePredicate(CardSubtype.DINOSAUR)),
                new DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING));
    }
}
