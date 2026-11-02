package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.b.BearerOfOverwhelmingTruths;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOI", collectorNumber = "54")
@CardRegistration(set = "SIR", collectorNumber = "59")
public class DaringSleuth extends Card {

    public DaringSleuth() {
        setBackFaceCard(new BearerOfOverwhelmingTruths());

        // When you sacrifice a Clue, transform this creature.
        addTrigger(Triggers.sacrificed(new PermanentHasSubtypePredicate(CardSubtype.CLUE), ObjectRelation.YOU_CONTROL),
                new TransformSelfEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "BearerOfOverwhelmingTruths";
    }
}
