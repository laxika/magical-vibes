package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

@CardRegistration(set = "WTH", collectorNumber = "22")
public class Peacekeeper extends Card {

    public Peacekeeper() {
        // At the beginning of your upkeep, sacrifice this creature unless you pay {1}{W}.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ForcedCostOrElseEffect(
                        new PayManaCost("{1}{W}"),
                        List.of(new SacrificeSelfEffect()),
                        true));
        // Creatures can't attack. Expressed as a global attack lock with an exemption
        // predicate that no creature can ever match.
        addEffect(EffectSlot.STATIC,
                new CreaturesCantAttackUnlessPredicateEffect(
                        new PermanentNotPredicate(new PermanentTruePredicate())));
    }
}
