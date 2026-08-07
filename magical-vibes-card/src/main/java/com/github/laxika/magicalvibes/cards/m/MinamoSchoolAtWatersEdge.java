package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "279")
public class MinamoSchoolAtWatersEdge extends Card {

    public MinamoSchoolAtWatersEdge() {
        // {T}: Add {U}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.BLUE)),
                "{T}: Add {U}."
        ));

        // {U}, {T}: Untap target legendary permanent.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}",
                List.of(new UntapPermanentsEffect(
                        TapUntapScope.TARGET, new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY))),
                "{U}, {T}: Untap target legendary permanent.",
                new PermanentPredicateTargetFilter(
                        new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY),
                        "Target must be a legendary permanent"
                )
        ));
    }
}
