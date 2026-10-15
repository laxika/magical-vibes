package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.Set;

@CardRegistration(set = "THS", collectorNumber = "15")
public class GlareOfHeresy extends Card {

    public GlareOfHeresy() {
        target(new PermanentPredicateTargetFilter(
                new PermanentColorInPredicate(Set.of(CardColor.WHITE)),
                "Target must be a white permanent"
        )).addEffect(EffectSlot.SPELL, new ExileTargetPermanentEffect());
    }
}
