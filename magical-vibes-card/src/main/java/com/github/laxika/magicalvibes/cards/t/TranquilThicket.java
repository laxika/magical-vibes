package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

@CardRegistration(set = "ONS", collectorNumber = "326")
@CardRegistration(set = "DD1", collectorNumber = "27")
@CardRegistration(set = "DDJ", collectorNumber = "82")
@CardRegistration(set = "VMA", collectorNumber = "320")
@CardRegistration(set = "EVG", collectorNumber = "26")
@CardRegistration(set = "DDU", collectorNumber = "29")
@CardRegistration(set = "MH1", collectorNumber = "248")
@CardRegistration(set = "HA2", collectorNumber = "25")
@CardRegistration(set = "C13", collectorNumber = "329")
@CardRegistration(set = "CMD", collectorNumber = "292")
@CardRegistration(set = "C14", collectorNumber = "316")
@CardRegistration(set = "LTC", collectorNumber = "341")
public class TranquilThicket extends Card {

    public TranquilThicket() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
        addCycling("{G}");
    }
}
