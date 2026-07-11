package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "113")
public class FinalRevels extends Card {

    public FinalRevels() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("All creatures get +2/+0 until end of turn", new BoostAllCreaturesEffect(2, 0)),
                new ChooseOneEffect.ChooseOneOption("All creatures get -0/-2 until end of turn", new BoostAllCreaturesEffect(0, -2))
        )));
    }
}
