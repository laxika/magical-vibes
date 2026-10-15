package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToLifeLostWhenEnteredEffect;
import com.github.laxika.magicalvibes.model.effect.LoseAllButOneLifeEffect;

@CardRegistration(set = "JUD", collectorNumber = "99")
public class SoulgorgerOrgg extends Card {

    public SoulgorgerOrgg() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new LoseAllButOneLifeEffect());
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new GainLifeEqualToLifeLostWhenEnteredEffect());
    }
}
