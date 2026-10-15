package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CanBlockOnlyIfAttackerMatchesPredicateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "AVR", collectorNumber = "110")
public class HuntedGhoul extends Card {

    public HuntedGhoul() {
        // "This creature can't block Humans." Modelled as the exact complement: it can only
        // block attackers that aren't Humans.
        addEffect(EffectSlot.STATIC, new CanBlockOnlyIfAttackerMatchesPredicateEffect(
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.HUMAN)),
                "creatures that aren't Humans"
        ));
    }
}
