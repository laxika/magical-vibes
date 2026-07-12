package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "9ED", collectorNumber = "68")
@CardRegistration(set = "8ED", collectorNumber = "69")
public class Confiscate extends Card {

    public Confiscate() {
        target(new PermanentPredicateTargetFilter(
                new PermanentTruePredicate(),
                "Target must be a permanent"
        )).addEffect(EffectSlot.STATIC, new ControlEnchantedCreatureEffect());
    }
}
