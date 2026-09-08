package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsOtherPermanentCount;
import com.github.laxika.magicalvibes.model.effect.CantBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.Set;

@CardRegistration(set = "SOI", collectorNumber = "164")
public class HowlpackWolf extends Card {

    public HowlpackWolf() {
        // This creature can't block unless you control another Wolf or Werewolf.
        addEffect(EffectSlot.STATIC, new CantBlockUnlessEffect(
                new ControlsOtherPermanentCount(1,
                        new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.WOLF, CardSubtype.WEREWOLF))),
                "you control another Wolf or Werewolf"
        ));
    }
}
