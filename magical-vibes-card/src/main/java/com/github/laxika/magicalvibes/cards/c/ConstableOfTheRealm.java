package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.RenownEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "10")
public class ConstableOfTheRealm extends Card {

    public ConstableOfTheRealm() {
        // Renown 2
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new RenownEffect(2));

        // Whenever one or more +1/+1 counters are put on this creature, exile up to one other
        // target nonland permanent until this creature leaves the battlefield.
        PermanentPredicate otherNonlandPermanent = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));
        target(new PermanentPredicateTargetFilter(
                otherNonlandPermanent,
                "Target must be another nonland permanent"
        ), 0, 1).addEffect(EffectSlot.ON_SELF_PLUS_ONE_PLUS_ONE_COUNTERS_PUT,
                new ExileTargetPermanentUntilSourceLeavesEffect(false, otherNonlandPermanent));
    }
}
