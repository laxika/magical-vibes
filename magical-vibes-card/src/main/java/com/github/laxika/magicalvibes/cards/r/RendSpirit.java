package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "CHK", collectorNumber = "141")
public class RendSpirit extends Card {

    public RendSpirit() {
        target(new PermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.SPIRIT),
                "Target must be a Spirit"
        ))
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
