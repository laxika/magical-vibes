package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "WWK", collectorNumber = "53")
public class ButcherOfMalakir extends Card {

    public ButcherOfMalakir() {
        SacrificePermanentsEffect effect = new SacrificePermanentsEffect(
                1, new PermanentIsCreaturePredicate(), SacrificeRecipient.EACH_OPPONENT);
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, effect);
        addEffect(EffectSlot.ON_DEATH, effect);
    }
}
