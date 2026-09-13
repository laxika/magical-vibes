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
public class ForgottenCave extends Card {

    public ForgottenCave() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));
        addCycling("{R}");
    }
}
