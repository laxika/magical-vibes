package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ELD", collectorNumber = "232")
public class SorcerersBroom extends Card {

    public SorcerersBroom() {
        addTrigger(Triggers.sacrificed(
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate()),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                new MayPayManaEffect(
                                "{3}",
                                new CreateTokenCopyOfSourceEffect(),
                                "Pay {3} to create a token that's a copy of Sorcerer's Broom?"
                        ));
    }
}
