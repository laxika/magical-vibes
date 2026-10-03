package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesPermanentsThenPhaseOutRestEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentsCantPhaseInEffect;

@CardRegistration(set = "BRC", collectorNumber = "21")
@CardRegistration(set = "BRC", collectorNumber = "41")
public class DiscipleOfCaelusNin extends Card {

    public DiscipleOfCaelusNin() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EachPlayerChoosesPermanentsThenPhaseOutRestEffect(5));
        addEffect(EffectSlot.STATIC, new PermanentsCantPhaseInEffect());
    }
}
