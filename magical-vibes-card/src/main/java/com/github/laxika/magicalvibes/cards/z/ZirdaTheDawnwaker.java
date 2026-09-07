package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceNonManaActivatedAbilityCostEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "233")
public class ZirdaTheDawnwaker extends Card {

    public ZirdaTheDawnwaker() {
        addEffect(EffectSlot.STATIC, new ReduceNonManaActivatedAbilityCostEffect(2));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new CantBlockThisTurnEffect(TapUntapScope.TARGET)),
                "{1}, {T}: Target creature can't block this turn.",
                TargetFilters.creature()));
    }
}
