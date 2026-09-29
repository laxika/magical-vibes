package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.LookAtTopTwoPlanarCardsThenPlaneswalkReplacementEffect;

@CardRegistration(set = "WHO", collectorNumber = "110")
public class SusanForeman extends Card {

    public SusanForeman() {
        addEffect(EffectSlot.STATIC, new LookAtTopTwoPlanarCardsThenPlaneswalkReplacementEffect());
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
    }
}
