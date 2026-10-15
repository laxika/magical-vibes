package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.SourceIntensity;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.IntensifyNamedCardsEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;

@CardRegistration(set = "HBG", collectorNumber = "74")
public class MintharaOfTheAbsolute extends Card {

    private static final String CARD_NAME = "Minthara of the Absolute";

    public MintharaOfTheAbsolute() {
        OncePerTurnTriggerEffect intensify =
                new OncePerTurnTriggerEffect(new IntensifyNamedCardsEffect(CARD_NAME));
        addEffect(EffectSlot.STATIC,
                new DynamicStaticBoostEffect(new SourceIntensity(), new Fixed(0), GrantScope.ALL_OWN_CREATURES));
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, intensify);
        addEffect(EffectSlot.ON_ALLY_PERMANENT_LEAVES_BATTLEFIELD, intensify);
    }
}
