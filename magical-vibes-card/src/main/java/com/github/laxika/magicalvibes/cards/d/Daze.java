package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ReturnPermanentsCost;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "NEM", collectorNumber = "30")
public class Daze extends Card {

    public Daze() {
        addCastingOption(new AlternateHandCast(List.of(
                new ReturnPermanentsCost(1, new PermanentHasSubtypePredicate(CardSubtype.ISLAND))
        )));
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
