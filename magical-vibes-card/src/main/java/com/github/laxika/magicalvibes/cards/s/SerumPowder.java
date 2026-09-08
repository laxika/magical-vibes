package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.SerumPowderEffect;

@CardRegistration(set = "DST", collectorNumber = "138")
public class SerumPowder extends Card {

    public SerumPowder() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        addEffect(EffectSlot.MULLIGAN_ACTION, new SerumPowderEffect());
    }
}
