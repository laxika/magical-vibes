package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "THS", collectorNumber = "228")
@CardRegistration(set = "M20", collectorNumber = "257")
@CardRegistration(set = "M21", collectorNumber = "256")
@CardRegistration(set = "WHO", collectorNumber = "321")
@CardRegistration(set = "WHO", collectorNumber = "530")
@CardRegistration(set = "WHO", collectorNumber = "912")
@CardRegistration(set = "WHO", collectorNumber = "1121")
@CardRegistration(set = "PIP", collectorNumber = "312")
@CardRegistration(set = "PIP", collectorNumber = "525")
@CardRegistration(set = "PIP", collectorNumber = "840")
@CardRegistration(set = "PIP", collectorNumber = "1053")
@CardRegistration(set = "SOC", collectorNumber = "417")
@CardRegistration(set = "C21", collectorNumber = "327")
@CardRegistration(set = "MKC", collectorNumber = "306")
@CardRegistration(set = "BLC", collectorNumber = "344")
public class TempleOfTriumph extends Card {

    public TempleOfTriumph() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(1));

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));
    }
}
