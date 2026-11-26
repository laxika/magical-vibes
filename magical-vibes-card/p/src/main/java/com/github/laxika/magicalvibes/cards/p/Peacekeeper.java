package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "WTH", collectorNumber = "22")
public class Peacekeeper extends Card {

    public Peacekeeper() {
        // At the beginning of your upkeep, sacrifice this creature unless you pay {1}{W}.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                UnlessPaysEffect.unlessYouPay("{1}{W}", new SacrificeSelfEffect()));
        // Creatures can't attack. Expressed as a global attack lock with an exemption
        // predicate that no creature can ever match.
        addStaticEffect(new CreaturesCantAttackUnlessPredicateEffect(
                        new PermanentNotPredicate(new PermanentTruePredicate())));
    }
}
