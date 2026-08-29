package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneForTargetCreatureEffect;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "46")
public class ShaperParasite extends Card {

    public ShaperParasite() {
        addMorph("{2}{U}");
        addEffect(EffectSlot.ON_TURNED_FACE_UP, new ChooseOneForTargetCreatureEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Gets +2/-2", new BoostTargetCreatureEffect(2, -2)),
                new ChooseOneEffect.ChooseOneOption("Gets -2/+2", new BoostTargetCreatureEffect(-2, 2)))));
    }
}
