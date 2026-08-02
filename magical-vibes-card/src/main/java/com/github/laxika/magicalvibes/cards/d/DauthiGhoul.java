package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;

@CardRegistration(set = "TMP", collectorNumber = "121")
public class DauthiGhoul extends Card {

    public DauthiGhoul() {
        // Whenever a creature with shadow dies, put a +1/+1 counter on Dauthi Ghoul.
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES,
                new TriggeringCardConditionalEffect(
                        new CardKeywordPredicate(Keyword.SHADOW),
                        new PutCountersOnSourceEffect(1, 1, 1)));
    }
}
