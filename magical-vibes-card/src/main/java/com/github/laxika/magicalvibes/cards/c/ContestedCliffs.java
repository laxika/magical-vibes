package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.FightTargetsEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "314")
public class ContestedCliffs extends Card {

    public ContestedCliffs() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{R}{G}",
                List.of(new FightTargetsEffect()),
                "{R}{G}, {T}: Target Beast creature you control fights target creature an opponent controls.",
                List.of(
                        new ControlledPermanentPredicateTargetFilter(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentHasSubtypePredicate(CardSubtype.BEAST)
                                )),
                                "Target must be a Beast creature you control"
                        ),
                        TargetFilters.creatureAnOpponentControls()
                ),
                2,
                2
        ));
    }
}
