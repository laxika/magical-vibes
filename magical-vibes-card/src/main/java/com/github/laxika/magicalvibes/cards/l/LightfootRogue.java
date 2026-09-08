package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RollD20Effect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "AFR", collectorNumber = "111")
public class LightfootRogue extends Card {

    public LightfootRogue() {
        addEffect(EffectSlot.ON_ATTACK, new RollD20Effect(
                new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.SELF),
                SequenceEffect.of(
                        new BoostSelfEffect(1, 0),
                        new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.SELF)),
                SequenceEffect.of(
                        new BoostSelfEffect(3, 0),
                        new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF),
                        new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.SELF))));
    }
}
