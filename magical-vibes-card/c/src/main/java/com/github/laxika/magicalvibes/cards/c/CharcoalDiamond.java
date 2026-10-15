package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;


@CardRegistration(set = "7ED", collectorNumber = "289")
@CardRegistration(set = "MIR", collectorNumber = "296")
@CardRegistration(set = "6ED", collectorNumber = "276")
@CardRegistration(set = "C14", collectorNumber = "235")
@CardRegistration(set = "MIC", collectorNumber = "158")
@CardRegistration(set = "VOC", collectorNumber = "162")
@CardRegistration(set = "FDC", collectorNumber = "248")
public class CharcoalDiamond extends Card {

    public CharcoalDiamond() {
        // Charcoal Diamond enters the battlefield tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {T}: Add {B}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));
    }
}
