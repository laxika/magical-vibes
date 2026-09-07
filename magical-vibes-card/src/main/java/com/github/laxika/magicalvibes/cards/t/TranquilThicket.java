package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

@CardRegistration(set = "ONS", collectorNumber = "326")
public class TranquilThicket extends Card {

    public TranquilThicket() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
        addCycling("{G}");
    }
}
