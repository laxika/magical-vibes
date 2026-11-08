package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ODY", collectorNumber = "180")
public class BurningSands extends Card {

    public BurningSands() {
        // Whenever a creature dies, that creature's controller sacrifices a land of their choice.
        addTrigger(Triggers.creatureDies(ObjectRelation.ANY),
                new SacrificePermanentsEffect(1, new PermanentIsLandPredicate(),
                        SacrificeRecipient.TRIGGERING_PERMANENT_CONTROLLER));
    }
}
