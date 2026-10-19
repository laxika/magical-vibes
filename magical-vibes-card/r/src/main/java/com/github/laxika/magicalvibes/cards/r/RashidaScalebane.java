package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.TargetPower;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "35")
public class RashidaScalebane extends Card {

    public RashidaScalebane() {
        // Destroy first, then gain life equal to the destroyed Dragon's last-known power, so that a
        // life-total restriction on the Dragon (Platinum Emperion) has already ended when life is gained.
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new DestroyTargetPermanentEffect(true), new GainLifeEffect(new TargetPower())),
                "{T}: Destroy target attacking or blocking Dragon. It can't be regenerated. You gain life equal to its power.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.DRAGON),
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsAttackingPredicate(),
                                        new PermanentIsBlockingPredicate())))),
                        "Target must be an attacking or blocking Dragon")));
    }
}
