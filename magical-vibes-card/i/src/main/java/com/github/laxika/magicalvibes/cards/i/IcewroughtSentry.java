package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WOE", collectorNumber = "55")
public class IcewroughtSentry extends Card {

    public IcewroughtSentry() {
        target(TargetFilters.creatureAnOpponentControls()).addTrigger(Triggers.attacks(),
                new MayPayManaEffect("{1}{U}",
                        new QueueReflexiveAbilityEffect(new TapPermanentsEffect(TapUntapScope.TARGET)),
                        "Pay {1}{U} to tap target creature an opponent controls?"));
        addTrigger(Triggers.youTapPermanentOpponentControls(),
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsCreaturePredicate(),
                        new BoostSelfEffect(2, 1)));
    }
}
