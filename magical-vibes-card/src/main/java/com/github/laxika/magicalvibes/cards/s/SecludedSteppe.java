package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

@CardRegistration(set = "ONS", collectorNumber = "324")
@CardRegistration(set = "C13", collectorNumber = "319")
@CardRegistration(set = "DDC", collectorNumber = "25")
@CardRegistration(set = "VMA", collectorNumber = "314")
@CardRegistration(set = "DDO", collectorNumber = "29")
@CardRegistration(set = "DVD", collectorNumber = "25")
@CardRegistration(set = "MH1", collectorNumber = "245")
@CardRegistration(set = "HA2", collectorNumber = "24")
@CardRegistration(set = "CMD", collectorNumber = "286")
@CardRegistration(set = "C14", collectorNumber = "310")
@CardRegistration(set = "C15", collectorNumber = "304")
public class SecludedSteppe extends Card {

    public SecludedSteppe() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));
        addCycling("{W}");
    }
}
