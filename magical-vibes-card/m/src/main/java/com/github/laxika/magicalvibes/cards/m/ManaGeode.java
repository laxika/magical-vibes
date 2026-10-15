package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "WAR", collectorNumber = "241")
public class ManaGeode extends Card {

    public ManaGeode() {
        // When this artifact enters, scry 1.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(1));

        // {T}: Add one mana of any color.
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
