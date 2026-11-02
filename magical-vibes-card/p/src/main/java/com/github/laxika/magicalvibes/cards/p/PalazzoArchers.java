package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTriggeringAttackerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ACR", collectorNumber = "42")
public class PalazzoArchers extends Card {

    public PalazzoArchers() {
        addTrigger(Triggers.creatureAttacksYouOrYourPlaneswalker(),
                new DealDamageToTriggeringAttackerEffect(
                        new SourcePower(), new PermanentHasKeywordPredicate(Keyword.FLYING)));
    }
}
