package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "FIN", collectorNumber = "271")
public class AdventurersInn extends Card {

    public AdventurersInn() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(2));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
    }
}
