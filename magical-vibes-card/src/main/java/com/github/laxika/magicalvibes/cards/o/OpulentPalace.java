package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

@CardRegistration(set = "KTK", collectorNumber = "238")
@CardRegistration(set = "TDM", collectorNumber = "264")
@CardRegistration(set = "ECC", collectorNumber = "157")
@CardRegistration(set = "C20", collectorNumber = "295")
@CardRegistration(set = "PIP", collectorNumber = "278")
@CardRegistration(set = "PIP", collectorNumber = "806")
public class OpulentPalace extends Card {

    public OpulentPalace() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));
    }
}
