package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.b.BearerOfOverwhelmingTruths;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "SOI", collectorNumber = "54")
public class DaringSleuth extends Card {

    public DaringSleuth() {
        setBackFaceCard(new BearerOfOverwhelmingTruths());

        // When you sacrifice a Clue, transform this creature.
        addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.CLUE),
                        new TransformSelfEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "BearerOfOverwhelmingTruths";
    }
}
