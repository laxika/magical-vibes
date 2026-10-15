package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.WillOfThePlaneswalkersEffect;

@CardRegistration(set = "MOC", collectorNumber = "34")
@CardRegistration(set = "MOC", collectorNumber = "121")
public class PathOfThePyromancer extends Card {

    public PathOfThePyromancer() {
        addEffect(EffectSlot.SPELL, new DiscardHandEffect());
        addEffect(EffectSlot.SPELL, new AwardManaEffect(ManaColor.RED, new EventValue()));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(new Sum(new EventValue(), new Fixed(1))));
        addEffect(EffectSlot.SPELL, new WillOfThePlaneswalkersEffect());
    }
}
