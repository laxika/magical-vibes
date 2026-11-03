package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "CMM", collectorNumber = "78")
public class BrinelinTheMoonKraken extends Card {

    public BrinelinTheMoonKraken() {
        // Both abilities target a nonland permanent: one target group, each ability choosing its own target.
        target(TargetFilters.nonlandPermanent())
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new MayEffect(ReturnToHandEffect.target(),
                                "Return target nonland permanent to its owner's hand?"))
                .addTrigger(Triggers.youCastSpell().withFilter(new CardMinManaValuePredicate(6, true)),
                        new MayEffect(ReturnToHandEffect.target(),
                                "Return target nonland permanent to its owner's hand?"));
    }
}
