package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "VOW", collectorNumber = "138")
public class WeddingSecurity extends Card {

    public WeddingSecurity() {
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                new SacrificePermanentThenEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.BLOOD),
                        SequenceEffect.of(
                                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                                new DrawCardEffect()),
                        "a Blood token", false, false),
                "Sacrifice a Blood token?"));
    }
}
