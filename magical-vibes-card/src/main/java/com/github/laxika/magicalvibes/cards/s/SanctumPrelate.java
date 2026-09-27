package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseNumberOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.NoncreatureSpellsWithChosenManaValueCantBeCastEffect;

@CardRegistration(set = "SLD", collectorNumber = "278")
public class SanctumPrelate extends Card {

    public SanctumPrelate() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseNumberOnEnterEffect(0, 20));
        addEffect(EffectSlot.STATIC, new NoncreatureSpellsWithChosenManaValueCantBeCastEffect());
    }
}
