package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "TLA", collectorNumber = "230")
public class KataraTheFearless extends Card {

    public KataraTheFearless() {
        addEffect(EffectSlot.STATIC,
                new AdditionalTriggeredAbilityEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.ALLY)));
    }
}
