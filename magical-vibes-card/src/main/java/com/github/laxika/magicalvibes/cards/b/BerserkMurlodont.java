package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CreaturesBlockingSource;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "LGN", collectorNumber = "117")
public class BerserkMurlodont extends Card {

    public BerserkMurlodont() {
        // Whenever a Beast becomes blocked, that Beast gets +1/+1 until end of turn for each
        // creature blocking it.
        addEffect(EffectSlot.ON_ANY_CREATURE_BECOMES_BLOCKED,
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.BEAST),
                        new BoostSelfEffect(new CreaturesBlockingSource(), new CreaturesBlockingSource())));
    }
}
