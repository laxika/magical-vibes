package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OTJ", collectorNumber = "168")
public class HardbristleBandit extends Card {

    public HardbristleBandit() {
        addActivatedAbility(ManaAbilities.tapForAnyColor());
        addTrigger(Triggers.youCommitCrime(),
                new OncePerTurnTriggerEffect(new UntapPermanentsEffect(TapUntapScope.SELF)));
    }
}
