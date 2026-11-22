package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileCardsWithSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "BOK", collectorNumber = "20")
@CardRegistration(set = "UDS", collectorNumber = "18")
public class Scour extends Card {

    public Scour() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsEnchantmentPredicate(),
                "Target must be an enchantment"
        )).addSpellEffect(new ExileTargetPermanentEffect(new PermanentIsEnchantmentPredicate()))
                .addSpellEffect(ExileCardsWithSameNameEffect.all(
                        Subjects.target(), Subjects.controllerOf(Subjects.target())));
    }
}
