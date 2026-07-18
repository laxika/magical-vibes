package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "4ED", collectorNumber = "112")
public class VolcanicEruption extends Card {

    public VolcanicEruption() {
        // Destroy X target Mountains. ~ deals damage to each creature and each player equal to the
        // number of Mountains put into a graveyard this way. The destroy effect snapshots the count
        // actually destroyed onto the entry's event value, which the mass damage reads via EventValue.
        targetX(new PermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN),
                "Targets must be Mountains"
        ), 100).addEffect(EffectSlot.SPELL, new DestroyEachTargetPermanentEffect());

        addEffect(EffectSlot.SPELL, new MassDamageEffect(new EventValue(), true));
    }
}
