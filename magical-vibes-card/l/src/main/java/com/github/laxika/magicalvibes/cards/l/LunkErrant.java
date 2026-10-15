package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "MOR", collectorNumber = "94")
public class LunkErrant extends Card {

    public LunkErrant() {
        addEffect(EffectSlot.ON_ATTACK,
                new ConditionalEffect(new AttacksAlone(), new BoostSelfEffect(1, 1)));
        addEffect(EffectSlot.ON_ATTACK,
                new ConditionalEffect(new AttacksAlone(), new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF)));
    }
}
