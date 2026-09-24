package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.IgnoreLegendRuleForControlledPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "1541")
public class SakashimaOfAThousandFaces extends Card {

    public SakashimaOfAThousandFaces() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CopyPermanentOnEnterEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentControlledBySourceControllerPredicate()
                )),
                "creature you control",
                null, null, Set.of(), List.of(), null, null, false, null, Set.of(),
                Map.of(EffectSlot.STATIC, List.<CardEffect>of(
                        new IgnoreLegendRuleForControlledPermanentsEffect()
                )),
                false, false, null, Set.of(), Set.of(Keyword.PARTNER), false, true, Set.of(), false, false, null, false
        ));
        addEffect(EffectSlot.STATIC, new IgnoreLegendRuleForControlledPermanentsEffect());
    }
}
