package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToRecipientsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "82")
public class GoblinFurrier extends Card {

    public GoblinFurrier() {
        addStaticEffect(PreventDamageToRecipientsEffect.to(Subjects.controlledBy(Subjects.eachPlayer(),
                        new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(),
                                new PermanentHasSupertypePredicate(CardSupertype.SNOW)))))
                .from(new PermanentIsSourcePermanentPredicate()));
    }
}
