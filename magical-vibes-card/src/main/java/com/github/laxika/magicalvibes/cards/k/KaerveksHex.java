package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.Set;

@CardRegistration(set = "MIR", collectorNumber = "130")
public class KaerveksHex extends Card {

    public KaerveksHex() {
        // "Kaervek's Hex deals 1 damage to each nonblack creature and an additional 1 damage to
        // each green creature." Two mass-damage effects: a green nonblack creature takes 2, a
        // black-green creature only the additional 1. MassDamageEffect already limits to creatures.
        addEffect(EffectSlot.SPELL, new MassDamageEffect(
                1, false, false,
                new PermanentNotPredicate(new PermanentColorInPredicate(Set.of(CardColor.BLACK)))));

        addEffect(EffectSlot.SPELL, new MassDamageEffect(
                1, false, false,
                new PermanentColorInPredicate(Set.of(CardColor.GREEN))));
    }
}
