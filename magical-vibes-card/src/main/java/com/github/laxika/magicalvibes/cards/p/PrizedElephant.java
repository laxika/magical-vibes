package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "M13", collectorNumber = "27")
public class PrizedElephant extends Card {

    public PrizedElephant() {
        // This creature gets +1/+1 as long as you control a Forest.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.FOREST)),
                new StaticBoostEffect(1, 1, GrantScope.SELF)));

        // {G}: This creature gains trample until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{G}",
                List.of(new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF)),
                "{G}: This creature gains trample until end of turn."));
    }
}
