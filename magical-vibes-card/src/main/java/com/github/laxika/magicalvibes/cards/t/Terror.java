package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "10E", collectorNumber = "182")
@CardRegistration(set = "6ED", collectorNumber = "160")
@CardRegistration(set = "5ED", collectorNumber = "196")
@CardRegistration(set = "4ED", collectorNumber = "164")
@CardRegistration(set = "MRD", collectorNumber = "79")
@CardRegistration(set = "ITP", collectorNumber = "25")
@CardRegistration(set = "RQS", collectorNumber = "24")
@CardRegistration(set = "ATH", collectorNumber = "27")
@CardRegistration(set = "BRB", collectorNumber = "87")
@CardRegistration(set = "BTD", collectorNumber = "33")
@CardRegistration(set = "SUM", collectorNumber = "132")
public class Terror extends Card {

    public Terror() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsArtifactPredicate()),
                        new PermanentNotPredicate(new PermanentColorInPredicate(Set.of(CardColor.BLACK)))
                )),
                "Target must be a nonartifact, nonblack creature"
        )).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect(true));
    }
}
