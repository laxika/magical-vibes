package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PlaneswalkEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "601")
public class TardisBay extends Card {

    public TardisBay() {
        addTrigger(Triggers.youCastSpell().withFilter(new CardMinManaValuePredicate(2, true)).nthThisTurn(1),
                new CascadeEffect());
        target(TargetFilters.artifact()).addTrigger(Triggers.chaosEnsues(),
                SequenceEffect.of(
                        new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                        new PlaneswalkEffect()));
    }
}
