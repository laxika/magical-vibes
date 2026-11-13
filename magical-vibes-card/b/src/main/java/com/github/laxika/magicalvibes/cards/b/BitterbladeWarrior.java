package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ExertAsItAttacksEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AKH", collectorNumber = "157")
@CardRegistration(set = "MB1", collectorNumber = "157")
public class BitterbladeWarrior extends Card {

    public BitterbladeWarrior() {
        addStaticEffect(new ExertAsItAttacksEffect());
        addTrigger(Triggers.exertedAsItAttacks(), SequenceEffect.of(
                                new BoostSelfEffect(1, 0),
                                new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.SELF)));
    }
}
