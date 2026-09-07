package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtLeastPredicate;

@CardRegistration(set = "DTK", collectorNumber = "207")
public class SightOfTheScalelords extends Card {

    public SightOfTheScalelords() {
        PermanentToughnessAtLeastPredicate toughnessAtLeastFour = new PermanentToughnessAtLeastPredicate(4);
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new BoostAllOwnCreaturesEffect(2, 2, toughnessAtLeastFour));
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.OWN_CREATURES, toughnessAtLeastFour));
    }
}
