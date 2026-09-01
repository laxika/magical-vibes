package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TapAndChooseOpponentGainsControlOfSourceAndMatchingPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;

import java.util.List;

@CardRegistration(set = "LEG", collectorNumber = "255")
public class RohgahhOfKherKeep extends Card {

    private static final String KOBOLDS_OF_KHER_KEEP = "Kobolds of Kher Keep";

    public RohgahhOfKherKeep() {
        var kobolds = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNamedPredicate(KOBOLDS_OF_KHER_KEEP)));

        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayPayManaEffect(
                "{R}{R}{R}",
                null,
                "Pay {R}{R}{R} to keep control of Rohgahh of Kher Keep?",
                new TapAndChooseOpponentGainsControlOfSourceAndMatchingPermanentsEffect(kobolds)));
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 2, GrantScope.OWN_CREATURES, kobolds));
    }
}
