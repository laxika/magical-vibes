package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WOE", collectorNumber = "55")
public class IcewroughtSentry extends Card {

    public IcewroughtSentry() {
        setCastTimeTargetFilter(TargetFilters.creatureAnOpponentControls());
        addEffect(EffectSlot.ON_ATTACK,
                MayPayManaEffect.reflexiveTarget("{1}{U}",
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        "Pay {1}{U} to tap target creature an opponent controls?"));
        addEffect(EffectSlot.ON_CONTROLLER_TAPS_OPPONENT_PERMANENT,
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsCreaturePredicate(),
                        new BoostSelfEffect(2, 1)));
    }
}
