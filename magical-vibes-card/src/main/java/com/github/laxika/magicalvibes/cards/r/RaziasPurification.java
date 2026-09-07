package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesDownToCountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "RAV", collectorNumber = "224")
public class RaziasPurification extends Card {

    public RaziasPurification() {
        addEffect(EffectSlot.SPELL,
                new EachPlayerSacrificesDownToCountEffect(3, new PermanentTruePredicate()));
    }
}
