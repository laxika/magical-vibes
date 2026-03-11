package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.Set;

@CardRegistration(set = "M11", collectorNumber = "130")
public class Combust extends Card {

    public Combust() {
        setCantBeCountered(true);
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(5, true));
        setTargetFilter(new PermanentPredicateTargetFilter(
                new PermanentColorInPredicate(Set.of(CardColor.WHITE, CardColor.BLUE)),
                "Target must be a white or blue creature."
        ));
    }
}
