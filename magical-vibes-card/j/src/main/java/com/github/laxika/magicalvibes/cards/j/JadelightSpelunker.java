package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;

@CardRegistration(set = "LCI", collectorNumber = "196")
@CardRegistration(set = "LCI", collectorNumber = "382")
@CardRegistration(set = "LCI", collectorNumber = "403")
public class JadelightSpelunker extends Card {

    public JadelightSpelunker() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExploreEffect(new XValue()));
    }
}
