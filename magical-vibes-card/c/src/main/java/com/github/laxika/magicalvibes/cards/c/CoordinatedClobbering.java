package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachTargetCreatureDealsPowerDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "173")
public class CoordinatedClobbering extends Card {

    public CoordinatedClobbering() {
        var victim = target(TargetFilters.creatureAnOpponentControls());
        var sources = target(new ControlledPermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsTappedPredicate())
                )),
                "Targets must be untapped creatures you control"
        ), 1, 2);

        sources.addEffect(EffectSlot.SPELL, new TapPermanentsEffect(TapUntapScope.TARGET));
        victim.addEffect(EffectSlot.SPELL, new EachTargetCreatureDealsPowerDamageToTargetCreatureEffect(1, 0));
    }
}
