package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.DiscardCardCastingCost;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "PCY", collectorNumber = "34")
public class Foil extends Card {

    public Foil() {
        addCastingOption(new AlternateHandCast(List.of(
                new DiscardCardCastingCost(new CardSubtypePredicate(CardSubtype.ISLAND), "an Island card"),
                new DiscardCardCastingCost(null, "another card"))));
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
