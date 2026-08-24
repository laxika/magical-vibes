package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "127")
public class Sangrophage extends Card {

    public Sangrophage() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ForcedCostOrElseEffect(
                        new PayManaCost("", null, false, 2),
                        List.of(new TapPermanentsEffect(TapUntapScope.SELF)),
                        true));
    }
}
