package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;


@CardRegistration(set = "AKH", collectorNumber = "281")
@CardRegistration(set = "M19", collectorNumber = "250")
@CardRegistration(set = "SOI", collectorNumber = "273")
@CardRegistration(set = "RIX", collectorNumber = "187")
public class ForsakenSanctuary extends Card {

    public ForsakenSanctuary() {
        // This land enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {T}: Add {W}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));

        // {T}: Add {B}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));
    }
}
