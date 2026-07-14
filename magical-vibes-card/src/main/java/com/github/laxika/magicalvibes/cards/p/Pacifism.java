package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "6ED", collectorNumber = "33")
@CardRegistration(set = "7ED", collectorNumber = "29")
@CardRegistration(set = "8ED", collectorNumber = "33")
@CardRegistration(set = "9ED", collectorNumber = "31")
@CardRegistration(set = "10E", collectorNumber = "31")
@CardRegistration(set = "M10", collectorNumber = "22")
@CardRegistration(set = "M11", collectorNumber = "23")
public class Pacifism extends Card {

    public Pacifism() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
                .addEffect(EffectSlot.STATIC, new EnchantedCreatureCantAttackOrBlockEffect());
    }
}
