package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "MKM", collectorNumber = "266")
public class RaucousTheater extends Card {

    public RaucousTheater() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SurveilEffect(1));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));
    }
}
