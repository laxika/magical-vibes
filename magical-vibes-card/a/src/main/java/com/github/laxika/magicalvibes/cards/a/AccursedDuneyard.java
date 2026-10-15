package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DRC", collectorNumber = "20")
@CardRegistration(set = "DRC", collectorNumber = "36")
public class AccursedDuneyard extends Card {

    public AccursedDuneyard() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new RegenerateEffect(true)),
                "{2}, {T}: Regenerate target Shade, Skeleton, Specter, Spirit, Vampire, Wraith, or Zombie.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasAnySubtypePredicate(Set.of(
                                        CardSubtype.SHADE,
                                        CardSubtype.SKELETON,
                                        CardSubtype.SPECTER,
                                        CardSubtype.SPIRIT,
                                        CardSubtype.VAMPIRE,
                                        CardSubtype.WRAITH,
                                        CardSubtype.ZOMBIE
                                )))),
                        "Target must be a Shade, Skeleton, Specter, Spirit, Vampire, Wraith, or Zombie"
                )
        ));
    }
}
