package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostFirstTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSecondTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "NPH", collectorNumber = "113")
public class LeechingBite extends Card {

    public LeechingBite() {
        setMinTargets(2);
        setMaxTargets(2);
        setMultiTargetFilters(List.of(
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "First target must be a creature"
                ),
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Second target must be another creature"
                )
        ));
        addEffect(EffectSlot.SPELL, new BoostFirstTargetCreatureEffect(1, 1));
        addEffect(EffectSlot.SPELL, new BoostSecondTargetCreatureEffect(-1, -1));
    }
}
