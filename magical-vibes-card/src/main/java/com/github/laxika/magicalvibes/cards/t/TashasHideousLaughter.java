package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachOpponentExilesTopUntilTotalManaValueEffect;

@CardRegistration(set = "AFR", collectorNumber = "78")
public class TashasHideousLaughter extends Card {

    public TashasHideousLaughter() {
        addEffect(EffectSlot.SPELL, new EachOpponentExilesTopUntilTotalManaValueEffect(20));
    }
}
