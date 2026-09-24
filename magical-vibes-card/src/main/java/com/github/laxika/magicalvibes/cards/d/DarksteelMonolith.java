package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AlternativeCostForSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsColorlessPredicate;

@CardRegistration(set = "SLZ", collectorNumber = "102")
@CardRegistration(set = "SLZ", collectorNumber = "223")
@CardRegistration(set = "SLZ", collectorNumber = "344")
@CardRegistration(set = "CMM", collectorNumber = "743")
@CardRegistration(set = "CMM", collectorNumber = "778")
public class DarksteelMonolith extends Card {

    public DarksteelMonolith() {
        addEffect(EffectSlot.STATIC, new AlternativeCostForSpellsEffect(
                "{0}", new CardIsColorlessPredicate(), null, true, true));
    }
}
