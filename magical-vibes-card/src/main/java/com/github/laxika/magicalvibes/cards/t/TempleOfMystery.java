package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "THS", collectorNumber = "226")
@CardRegistration(set = "M20", collectorNumber = "255")
@CardRegistration(set = "M21", collectorNumber = "254")
@CardRegistration(set = "CP1", collectorNumber = "6")
@CardRegistration(set = "WHO", collectorNumber = "318")
@CardRegistration(set = "WHO", collectorNumber = "528")
@CardRegistration(set = "WHO", collectorNumber = "909")
@CardRegistration(set = "WHO", collectorNumber = "1119")
@CardRegistration(set = "PIP", collectorNumber = "308")
@CardRegistration(set = "PIP", collectorNumber = "522")
@CardRegistration(set = "PIP", collectorNumber = "836")
@CardRegistration(set = "PIP", collectorNumber = "1050")
@CardRegistration(set = "40K", collectorNumber = "299")
@CardRegistration(set = "SOC", collectorNumber = "414")
@CardRegistration(set = "C21", collectorNumber = "324")
@CardRegistration(set = "DSC", collectorNumber = "311")
@CardRegistration(set = "MKC", collectorNumber = "303")
public class TempleOfMystery extends Card {

    public TempleOfMystery() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(1));

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));
    }
}
