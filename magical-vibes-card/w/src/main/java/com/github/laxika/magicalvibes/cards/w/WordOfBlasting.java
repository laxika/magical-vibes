package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.TargetManaValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "5ED", collectorNumber = "276")
@CardRegistration(set = "ICE", collectorNumber = "224")
@CardRegistration(set = "MMQ", collectorNumber = "228")
public class WordOfBlasting extends Card {

    public WordOfBlasting() {
        target(new PermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.WALL),
                "Target must be a Wall"
        ))
                .addSpellEffect(new DestroyTargetPermanentEffect(true))
                .addSpellEffect(DealDamageEffect.to(Subjects.controllerOf(Subjects.target()), new TargetManaValue()));
    }
}
