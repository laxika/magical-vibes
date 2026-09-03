package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "315")
public class DaruEncampment extends Card {

    private static final PermanentAllOfPredicate SOLDIER_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentHasSubtypePredicate(CardSubtype.SOLDIER)
    ));

    public DaruEncampment() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}",
                List.of(new BoostTargetCreatureEffect(1, 1, SOLDIER_CREATURE)),
                "{W}, {T}: Target Soldier creature gets +1/+1 until end of turn.",
                new PermanentPredicateTargetFilter(SOLDIER_CREATURE, "Target must be a Soldier creature")
        ));
    }
}
