package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CardsLookedAtWhileScrying;
import com.github.laxika.magicalvibes.model.condition.MinimumMatchingAttackers;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOC", collectorNumber = "203")
public class CelebornTheWise extends Card {

    public CelebornTheWise() {
        addTrigger(Triggers.youAttack(),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new MinimumMatchingAttackers(1, new PermanentHasSubtypePredicate(CardSubtype.ELF))),
                new ScryEffect(1));
        addTrigger(Triggers.youScry(), new BoostSelfEffect(
                new CardsLookedAtWhileScrying(), new CardsLookedAtWhileScrying()));
    }
}
