package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "XLN", collectorNumber = "89")
public class AnointedDeacon extends Card {

    public AnointedDeacon() {
        // At the beginning of combat on your turn, you may have target Vampire get +2/+0
        // until end of turn.
        target(new PermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE),
                "Target must be a Vampire"
        )).addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new MayEffect(
                new BoostTargetCreatureEffect(2, 0),
                "You may have target Vampire get +2/+0 until end of turn."
        ));
    }
}
