package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "6ED", collectorNumber = "103")
@CardRegistration(set = "7ED", collectorNumber = "108")
@CardRegistration(set = "8ED", collectorNumber = "112")
@CardRegistration(set = "10E", collectorNumber = "122")
@CardRegistration(set = "M10", collectorNumber = "79")
@CardRegistration(set = "M11", collectorNumber = "78")
@CardRegistration(set = "5ED", collectorNumber = "132")
@CardRegistration(set = "4ED", collectorNumber = "111")
@CardRegistration(set = "CON", collectorNumber = "37")
@CardRegistration(set = "HOU", collectorNumber = "54")
public class Unsummon extends Card {

    public Unsummon() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        )).addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());
    }
}
