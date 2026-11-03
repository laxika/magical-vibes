package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "VIS", collectorNumber = "138")
public class SuleimansLegacy extends Card {

    public SuleimansLegacy() {
        // When this enchantment enters, destroy all Djinns and Efreets. They can't be regenerated.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DestroyAllPermanentsEffect(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentHasSubtypePredicate(CardSubtype.DJINN),
                        new PermanentHasSubtypePredicate(CardSubtype.EFREET))),
                true));

        // Whenever a Djinn or Efreet enters, destroy it. It can't be regenerated.
        addTrigger(Triggers.enters(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.DJINN),
                                new PermanentHasSubtypePredicate(CardSubtype.EFREET))),
                        ObjectRelation.ANY),
                new DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING, true));
    }
}
