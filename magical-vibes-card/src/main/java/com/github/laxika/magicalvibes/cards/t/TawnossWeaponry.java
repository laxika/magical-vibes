package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureWhileSourceTappedEffect;
import com.github.laxika.magicalvibes.model.effect.MayNotUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "401")
public class TawnossWeaponry extends Card {

    public TawnossWeaponry() {
        // Static: "You may choose not to untap this artifact during your untap step."
        addEffect(EffectSlot.STATIC, new MayNotUntapDuringUntapStepEffect());

        // {2}, {T}: Target creature gets +1/+1 for as long as this artifact remains tapped.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new BoostTargetCreatureWhileSourceTappedEffect(1, 1)),
                "{2}, {T}: Target creature gets +1/+1 for as long as this artifact remains tapped.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"
                )
        ));
    }
}
