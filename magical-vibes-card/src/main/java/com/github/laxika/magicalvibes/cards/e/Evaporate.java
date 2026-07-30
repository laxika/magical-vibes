package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "HML", collectorNumber = "74")
public class Evaporate extends Card {

    public Evaporate() {
        // "Evaporate deals 1 damage to each white and/or blue creature." — the color
        // predicate matches a creature that is any of the listed colors, so multicolored
        // white-blue creatures are hit once.
        addEffect(EffectSlot.SPELL, new MassDamageEffect(
                1,
                false,
                false,
                new PermanentColorInPredicate(Set.of(CardColor.WHITE, CardColor.BLUE))
        ));
    }
}
