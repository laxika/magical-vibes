package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import java.util.List;

@CardRegistration(set = "EXO", collectorNumber = "53")
@CardRegistration(set = "TPR", collectorNumber = "84")
public class Carnophage extends Card {

    public Carnophage() {
        // At the beginning of your upkeep, tap this creature unless you pay 1 life.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ForcedCostOrElseEffect(
                        new PayManaCost("", null, false, 1),
                        List.of(new TapPermanentsEffect(TapUntapScope.SELF)),
                        true));
    }
}
