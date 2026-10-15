package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.RepeatedAdditionalCostCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "100a")
@CardRegistration(set = "ALL", collectorNumber = "100b")
public class TasteOfParadise extends Card {

    public TasteOfParadise() {
        addEffect(EffectSlot.SPELL, new RepeatableAdditionalManaCost(List.of("{1}{G}")));

        // 3 life plus 3 more for each additional {1}{G} paid; the payments buy no targets,
        // so the count is read back off the stack entry rather than from X.
        addEffect(EffectSlot.SPELL, new GainLifeEffect(new Sum(
                new Fixed(3),
                new Scaled(new RepeatedAdditionalCostCount("{1}{G}"), 3)
        )));
    }
}
