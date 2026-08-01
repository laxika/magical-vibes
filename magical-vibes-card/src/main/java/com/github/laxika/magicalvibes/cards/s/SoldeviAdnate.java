package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ALL", collectorNumber = "60a")
@CardRegistration(set = "ALL", collectorNumber = "60b")
public class SoldeviAdnate extends Card {

    public SoldeviAdnate() {
        // {T}, Sacrifice a black or artifact creature: Add an amount of {B} equal to the
        // sacrificed creature's mana value. The Adnate itself is a black creature, so it is
        // a legal sacrifice (excludeSource = false); the mana value is snapshotted at payment.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificePermanentCost(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentAnyOfPredicate(List.of(
                                                new PermanentColorInPredicate(Set.of(CardColor.BLACK)),
                                                new PermanentIsArtifactPredicate())))),
                                "Sacrifice a black or artifact creature", false, false, true, false),
                        new AwardManaEffect(ManaColor.BLACK, new XValue())),
                "{T}, Sacrifice a black or artifact creature: Add an amount of {B} equal to the sacrificed creature's mana value."
        ));
    }
}
