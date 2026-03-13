package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.Set;

@CardRegistration(set = "M10", collectorNumber = "7")
@CardRegistration(set = "M11", collectorNumber = "9")
public class CelestialPurge extends Card {

    public CelestialPurge() {
        setTargetFilter(new PermanentPredicateTargetFilter(
                new PermanentColorInPredicate(Set.of(CardColor.BLACK, CardColor.RED)),
                "Target must be a black or red permanent"
        ));
        addEffect(EffectSlot.SPELL, new ExileTargetPermanentEffect());
    }
}
