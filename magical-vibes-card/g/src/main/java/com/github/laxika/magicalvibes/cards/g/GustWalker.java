package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ExertAsItAttacksEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AKH", collectorNumber = "17")
@CardRegistration(set = "AKR", collectorNumber = "22")
@CardRegistration(set = "MB1", collectorNumber = "131")
public class GustWalker extends Card {

    public GustWalker() {
        addStaticEffect(new ExertAsItAttacksEffect());
        addTrigger(Triggers.exertedAsItAttacks(), SequenceEffect.of(
                                new BoostSelfEffect(1, 1),
                                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)));
    }
}
