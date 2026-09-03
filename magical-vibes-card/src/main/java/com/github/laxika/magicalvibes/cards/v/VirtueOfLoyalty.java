package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.ArdenvaleFealty;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "WOE", collectorNumber = "38")
public class VirtueOfLoyalty extends Card {

    public VirtueOfLoyalty() {
        setBackFaceCard(new ArdenvaleFealty());
        addCastingOption(new AdventureCast("{1}{W}"));
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, SequenceEffect.of(
                new PutCounterOnEachControlledPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate()),
                new UntapPermanentsEffect(TapUntapScope.CONTROLLED, new PermanentIsCreaturePredicate())));
    }

    @Override
    public String getBackFaceClassName() {
        return "ArdenvaleFealty";
    }
}
