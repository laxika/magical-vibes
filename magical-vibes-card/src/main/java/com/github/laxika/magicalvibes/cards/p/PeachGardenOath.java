package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "PTK", collectorNumber = "15")
@CardRegistration(set = "8ED", collectorNumber = "34")
public class PeachGardenOath extends Card {

    public PeachGardenOath() {
        // You gain 2 life for each creature you control.
        addEffect(EffectSlot.SPELL, new GainLifeEffect(new Scaled(
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER), 2)));
    }
}
