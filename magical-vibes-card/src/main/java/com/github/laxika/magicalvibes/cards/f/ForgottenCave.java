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
public class ForgottenCave extends Card {

    public ForgottenCave() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));
        addCycling("{R}");
    }
}
