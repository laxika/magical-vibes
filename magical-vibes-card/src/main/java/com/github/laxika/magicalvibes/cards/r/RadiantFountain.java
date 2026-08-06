package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "M15", collectorNumber = "245")
public class RadiantFountain extends Card {

    public RadiantFountain() {
        // When Radiant Fountain enters the battlefield, you gain 2 life.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(2));

        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
    }
}
