package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.IgnoreLegendRuleForControlledPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "TLE", collectorNumber = "18")
public class SakashimaOfAThousandFaces extends Card {

    public SakashimaOfAThousandFaces() {
        IgnoreLegendRuleForControlledPermanentsEffect legendRuleExemption =
                new IgnoreLegendRuleForControlledPermanentsEffect();
        addEffect(EffectSlot.STATIC, legendRuleExemption);
        // You may have Sakashima enter as a copy of another creature you control, except it has
        // Sakashima's other abilities.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CopyPermanentOnEnterEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentControlledBySourceControllerPredicate())), "creature you control", Set.of(),
                Map.of(EffectSlot.STATIC, List.<CardEffect>of(legendRuleExemption))));
    }
}
