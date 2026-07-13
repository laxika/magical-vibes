package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageCantReduceLifeBelowOneEffect;

@CardRegistration(set = "9ED", collectorNumber = "55")
@CardRegistration(set = "8ED", collectorNumber = "57")
@CardRegistration(set = "7ED", collectorNumber = "56")
public class Worship extends Card {

    public Worship() {
        addEffect(EffectSlot.STATIC, new DamageCantReduceLifeBelowOneEffect());
    }
}
