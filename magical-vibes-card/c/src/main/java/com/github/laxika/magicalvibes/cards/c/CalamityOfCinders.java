package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "BLC", collectorNumber = "23")
@CardRegistration(set = "BLC", collectorNumber = "57")
public class CalamityOfCinders extends Card {

    public CalamityOfCinders() {
        addEffect(EffectSlot.SPELL, new MassDamageEffect(
                6, false, false, new PermanentNotPredicate(new PermanentIsTappedPredicate())));
    }
}
