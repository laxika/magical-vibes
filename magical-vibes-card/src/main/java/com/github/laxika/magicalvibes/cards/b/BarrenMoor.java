package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

@CardRegistration(set = "ONS", collectorNumber = "312")
@CardRegistration(set = "DDC", collectorNumber = "58")
@CardRegistration(set = "DDJ", collectorNumber = "78")
@CardRegistration(set = "VMA", collectorNumber = "292")
@CardRegistration(set = "DVD", collectorNumber = "58")
@CardRegistration(set = "MH1", collectorNumber = "236")
@CardRegistration(set = "HA2", collectorNumber = "19")
@CardRegistration(set = "C13", collectorNumber = "277")
@CardRegistration(set = "CMD", collectorNumber = "266")
@CardRegistration(set = "C14", collectorNumber = "284")
@CardRegistration(set = "C15", collectorNumber = "277")
@CardRegistration(set = "C21", collectorNumber = "277")
@CardRegistration(set = "C19", collectorNumber = "229")
@CardRegistration(set = "C18", collectorNumber = "235")
@CardRegistration(set = "40K", collectorNumber = "266")
@CardRegistration(set = "DSC", collectorNumber = "262")
@CardRegistration(set = "BLC", collectorNumber = "292")
@CardRegistration(set = "CM2", collectorNumber = "237")
@CardRegistration(set = "ARC", collectorNumber = "124")
public class BarrenMoor extends Card {

    public BarrenMoor() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));
        addCycling("{B}");
    }
}
