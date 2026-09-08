package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RollD20Effect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "AFR", collectorNumber = "141")
public class EarthCultElemental extends Card {

    public EarthCultElemental() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new RollD20Effect(
                new SacrificePermanentsEffect(1, new PermanentTruePredicate(),
                        SacrificeRecipient.EACH_PLAYER),
                new SacrificePermanentsEffect(1, new PermanentTruePredicate(),
                        SacrificeRecipient.EACH_OPPONENT),
                new SacrificePermanentsEffect(2, new PermanentTruePredicate(),
                        SacrificeRecipient.EACH_OPPONENT)));
    }
}
