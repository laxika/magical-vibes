package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CreaturesBlockingSource;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "STH", collectorNumber = "130")
@CardRegistration(set = "TPR", collectorNumber = "213")
@CardRegistration(set = "TSB", collectorNumber = "101")
public class SpinedSliver extends Card {

    public SpinedSliver() {
        // Whenever a Sliver becomes blocked, that Sliver gets +1/+1 until end of turn
        // for each creature blocking it.
        addEffect(EffectSlot.ON_ANY_CREATURE_BECOMES_BLOCKED,
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.SLIVER),
                        new BoostSelfEffect(new CreaturesBlockingSource(), new CreaturesBlockingSource())));
    }
}
