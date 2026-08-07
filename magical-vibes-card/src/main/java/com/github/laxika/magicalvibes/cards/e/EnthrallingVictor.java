package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ORI", collectorNumber = "142")
public class EnthrallingVictor extends Card {

    public EnthrallingVictor() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                        new PermanentPowerAtMostPredicate(2)
                )),
                "Target must be a creature an opponent controls with power 2 or less"
        ));

        // When this creature enters, gain control of target creature an opponent controls
        // with power 2 or less until end of turn. Untap that creature. It gains haste until end of turn.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainControlOfTargetEffect(ControlDuration.END_OF_TURN));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new UntapPermanentsEffect(TapUntapScope.TARGET));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET));
    }
}
