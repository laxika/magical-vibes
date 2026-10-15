package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.Set;

@CardRegistration(set = "M15", collectorNumber = "97")
public class Festergloom extends Card {

    public Festergloom() {
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(-1, -1,
                new PermanentNotPredicate(new PermanentColorInPredicate(Set.of(CardColor.BLACK)))));
    }
}
