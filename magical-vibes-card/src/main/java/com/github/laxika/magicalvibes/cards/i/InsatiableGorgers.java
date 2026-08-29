package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;

@CardRegistration(set = "EMN", collectorNumber = "134")
public class InsatiableGorgers extends Card {

    public InsatiableGorgers() {
        // Madness {3}{R}
        addCastingOption(new MadnessCast("{3}{R}"));

        // This creature attacks each combat if able.
        addEffect(EffectSlot.STATIC, new MustAttackEffect());
    }
}
