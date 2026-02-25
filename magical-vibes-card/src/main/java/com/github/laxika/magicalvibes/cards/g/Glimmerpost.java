package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeForEachSubtypeOnBattlefieldEffect;

@CardRegistration(set = "SOM", collectorNumber = "227")
public class Glimmerpost extends Card {

    public Glimmerpost() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new GainLifeForEachSubtypeOnBattlefieldEffect(CardSubtype.LOCUS));
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.COLORLESS));
    }
}
