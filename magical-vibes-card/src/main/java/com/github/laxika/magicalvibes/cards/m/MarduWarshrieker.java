package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.Raid;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "KTK", collectorNumber = "117")
public class MarduWarshrieker extends Card {

    public MarduWarshrieker() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new Raid(),
                SequenceEffect.of(
                        new AwardManaEffect(ManaColor.RED),
                        new AwardManaEffect(ManaColor.WHITE),
                        new AwardManaEffect(ManaColor.BLACK))));
    }
}
