package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTriggeringAttackerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "ACR", collectorNumber = "42")
public class PalazzoArchers extends Card {

    public PalazzoArchers() {
        addEffect(EffectSlot.ON_CREATURE_ATTACKS_YOU,
                new DealDamageToTriggeringAttackerEffect(
                        new SourcePower(), new PermanentHasKeywordPredicate(Keyword.FLYING)));
    }
}
