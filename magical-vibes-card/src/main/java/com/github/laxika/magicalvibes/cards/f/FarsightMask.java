package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceUntapped;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "MRD", collectorNumber = "170")
public class FarsightMask extends Card {

    public FarsightMask() {
        addEffect(EffectSlot.ON_CONTROLLER_DEALT_DAMAGE_BY_OPPONENT,
                new ConditionalEffect(
                        new SourceUntapped(),
                        new MayEffect(new DrawCardEffect(), "Draw a card?")));
    }
}
