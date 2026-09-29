package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.l.LupariShield;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "WHO", collectorNumber = "106")
public class KarvanistaLoyalLupari extends Card {

    public KarvanistaLoyalLupari() {
        setBackFaceCard(new LupariShield());
        addCastingOption(new AdventureCast("{1}{G}"));
        addEffect(EffectSlot.ON_ATTACK, new PutCounterOnEachControlledPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE, 1,
                new PermanentHasSubtypePredicate(CardSubtype.HUMAN)));
    }

    @Override
    public String getBackFaceClassName() {
        return "LupariShield";
    }
}
