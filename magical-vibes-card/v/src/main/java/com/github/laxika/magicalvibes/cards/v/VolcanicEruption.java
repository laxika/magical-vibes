package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "4ED", collectorNumber = "112")
@CardRegistration(set = "SUM", collectorNumber = "89")
@CardRegistration(set = "3ED", collectorNumber = "89")
@CardRegistration(set = "2ED", collectorNumber = "89")
@CardRegistration(set = "LEA", collectorNumber = "88")
@CardRegistration(set = "LEB", collectorNumber = "89")
public class VolcanicEruption extends Card {

    public VolcanicEruption() {
        targetExactlyX(new PermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN),
                "Targets must be Mountains"
        ), 100).addSpellEffect(new DestroyEachTargetPermanentEffect());

        addSpellEffect(DealDamageEffect.toEachCreature(AmountThisWay.died(StepKind.DESTROY))
                .andTo(Subjects.eachPlayer()));
    }
}
