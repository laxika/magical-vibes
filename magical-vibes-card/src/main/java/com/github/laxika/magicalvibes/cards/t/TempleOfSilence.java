package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "THS", collectorNumber = "227")
@CardRegistration(set = "M20", collectorNumber = "256")
@CardRegistration(set = "M21", collectorNumber = "255")
public class TempleOfSilence extends Card {

    public TempleOfSilence() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(1));

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));
    }
}
