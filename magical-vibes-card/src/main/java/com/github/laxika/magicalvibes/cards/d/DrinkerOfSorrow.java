package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "LGN", collectorNumber = "66")
public class DrinkerOfSorrow extends Card {

    public DrinkerOfSorrow() {
        addEffect(EffectSlot.ON_SELF_DEALS_COMBAT_DAMAGE,
                new SacrificePermanentsEffect(1, new PermanentTruePredicate(), SacrificeRecipient.CONTROLLER));
    }
}
