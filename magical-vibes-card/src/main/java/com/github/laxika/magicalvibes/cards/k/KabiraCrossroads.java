package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "ZEN", collectorNumber = "216")
public class KabiraCrossroads extends Card {

    public KabiraCrossroads() {
        // This land enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // When this land enters, you gain 2 life.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(2));

        // {T}: Add {W}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));
    }
}
