package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

@CardRegistration(set = "ONS", collectorNumber = "317")
@CardRegistration(set = "DD1", collectorNumber = "57")
@CardRegistration(set = "DDJ", collectorNumber = "33")
@CardRegistration(set = "VMA", collectorNumber = "297")
@CardRegistration(set = "EVG", collectorNumber = "57")
@CardRegistration(set = "DDT", collectorNumber = "59")
@CardRegistration(set = "MB1", collectorNumber = "246")
@CardRegistration(set = "MH1", collectorNumber = "239")
@CardRegistration(set = "HA2", collectorNumber = "21")
@CardRegistration(set = "C13", collectorNumber = "289")
@CardRegistration(set = "CMD", collectorNumber = "273")
@CardRegistration(set = "C14", collectorNumber = "296")
@CardRegistration(set = "C15", collectorNumber = "284")
@CardRegistration(set = "M3C", collectorNumber = "343")
@CardRegistration(set = "C21", collectorNumber = "289")
@CardRegistration(set = "C20", collectorNumber = "274")
@CardRegistration(set = "40K", collectorNumber = "280")
@CardRegistration(set = "BLC", collectorNumber = "305")
@CardRegistration(set = "C19", collectorNumber = "243")
@CardRegistration(set = "ONC", collectorNumber = "153")
@CardRegistration(set = "C18", collectorNumber = "246")
@CardRegistration(set = "FDC", collectorNumber = "301")
public class ForgottenCave extends Card {

    public ForgottenCave() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));
        addCycling("{R}");
    }
}
