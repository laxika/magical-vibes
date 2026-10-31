package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "EOE", collectorNumber = "95")
public class Depressurize extends Card {

    public Depressurize() {
        target(TargetFilters.creature())
                .addSpellEffect(new BoostTargetCreatureEffect(-3, 0))
                .addSpellEffect(new ConditionalEffect(
                        new TargetPermanentMatches(new PermanentPowerAtMostPredicate(0)),
                        new DestroyTargetPermanentEffect()));
    }
}
