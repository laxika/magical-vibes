package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "IKO", collectorNumber = "80")
public class ChitteringHarvester extends Card {

    public ChitteringHarvester() {
        addEffect(EffectSlot.ON_SELF_MUTATES, new SacrificePermanentsEffect(
                1, new PermanentIsCreaturePredicate(), SacrificeRecipient.EACH_OPPONENT));
    }
}
