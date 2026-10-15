package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardAndConjureOpponentGraveyardDuplicateEffect;

@CardRegistration(set = "YDMU", collectorNumber = "17")
public class NantukoSlicer extends Card {

    public NantukoSlicer() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{B}"));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ReturnCardAndConjureOpponentGraveyardDuplicateEffect());
    }
}
