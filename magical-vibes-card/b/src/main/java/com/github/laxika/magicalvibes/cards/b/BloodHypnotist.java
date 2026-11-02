package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "145")
@CardRegistration(set = "DBL", collectorNumber = "412")
public class BloodHypnotist extends Card {

    public BloodHypnotist() {
        addStaticEffect(new CantBlockEffect());

        target(TargetFilters.creature()).addTrigger(Triggers.sacrificed(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsTokenPredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.BLOOD))),
                        ObjectRelation.YOU_CONTROL),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                new CantBlockThisTurnEffect(TapUntapScope.TARGET));
    }
}
