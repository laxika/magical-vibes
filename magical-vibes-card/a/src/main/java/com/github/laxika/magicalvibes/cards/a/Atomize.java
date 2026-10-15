package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PIP", collectorNumber = "94")
@CardRegistration(set = "PIP", collectorNumber = "622")
@CardRegistration(set = "PIP", collectorNumber = "338")
@CardRegistration(set = "PIP", collectorNumber = "866")
public class Atomize extends Card {

    public Atomize() {
        PermanentPredicateTargetFilter nonlandPermanent = TargetFilters.nonlandPermanent();
        target(nonlandPermanent)
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect(nonlandPermanent.predicate()))
                .addEffect(EffectSlot.SPELL, new ProliferateEffect());
    }
}
