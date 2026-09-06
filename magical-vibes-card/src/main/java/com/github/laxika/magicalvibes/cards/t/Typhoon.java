package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachOpponentEqualToControlledPermanentCountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "LEG", collectorNumber = "209")
public class Typhoon extends Card {

    public Typhoon() {
        addEffect(EffectSlot.SPELL, new DealDamageToEachOpponentEqualToControlledPermanentCountEffect(
                new PermanentHasSubtypePredicate(CardSubtype.ISLAND)));
    }
}
