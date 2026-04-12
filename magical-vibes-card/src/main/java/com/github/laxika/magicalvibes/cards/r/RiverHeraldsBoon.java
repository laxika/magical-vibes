package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "204")
public class RiverHeraldsBoon extends Card {

    public RiverHeraldsBoon() {
        setAllowSharedTargets(true);

        // Put a +1/+1 counter on target creature and a +1/+1 counter on up to one target Merfolk.

        // First target: any creature (mandatory)
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        )).addEffect(EffectSlot.SPELL, new PutPlusOnePlusOneCounterOnTargetCreatureEffect(1));

        // Second target: up to one Merfolk (optional)
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.MERFOLK)
                )),
                "Target must be a Merfolk"
        ), 0, 1).addEffect(EffectSlot.SPELL, new PutPlusOnePlusOneCounterOnTargetCreatureEffect(1));
    }
}
