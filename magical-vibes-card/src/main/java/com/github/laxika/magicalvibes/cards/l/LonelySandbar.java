package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

@CardRegistration(set = "ONS", collectorNumber = "320")
@CardRegistration(set = "DDT", collectorNumber = "27")
@CardRegistration(set = "DDJ", collectorNumber = "35")
@CardRegistration(set = "VMA", collectorNumber = "304")
@CardRegistration(set = "SLD", collectorNumber = "2161")
@CardRegistration(set = "SLD", collectorNumber = "2260")
@CardRegistration(set = "MH1", collectorNumber = "242")
@CardRegistration(set = "HA2", collectorNumber = "23")
@CardRegistration(set = "C13", collectorNumber = "305")
@CardRegistration(set = "CMD", collectorNumber = "281")
@CardRegistration(set = "C14", collectorNumber = "304")
@CardRegistration(set = "LTC", collectorNumber = "319")
public class LonelySandbar extends Card {

    public LonelySandbar() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));
        addCycling("{U}");
    }
}
