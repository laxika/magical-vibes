package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WOE", collectorNumber = "55")
public class IcewroughtSentry extends Card {

    public IcewroughtSentry() {
        target(TargetFilters.creatureAnOpponentControls()).addTrigger(Triggers.attacks(),
                UnlessPaysEffect.youMayPay("{1}{U}", new QueueReflexiveAbilityEffect(new TapPermanentsEffect(Subjects.target())))
                        .withPrompt("Pay {1}{U} to tap target creature an opponent controls?"));
        addTrigger(Triggers.youTapPermanentOpponentControls()
                        .withFilter(new PermanentIsCreaturePredicate()),
                BoostEffect.self(2, 1));
    }
}
