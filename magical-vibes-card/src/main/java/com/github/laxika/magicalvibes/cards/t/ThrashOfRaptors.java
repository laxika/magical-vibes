package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ControlsAnotherSubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "XLN", collectorNumber = "168")
public class ThrashOfRaptors extends Card {

    public ThrashOfRaptors() {
        // As long as you control another Dinosaur, this creature gets +2/+0 and has trample.
        addEffect(EffectSlot.STATIC, new ControlsAnotherSubtypeConditionalEffect(
                CardSubtype.DINOSAUR,
                new StaticBoostEffect(2, 0, GrantScope.SELF)
        ));
        addEffect(EffectSlot.STATIC, new ControlsAnotherSubtypeConditionalEffect(
                CardSubtype.DINOSAUR,
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF)
        ));
    }
}
