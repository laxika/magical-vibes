package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.BoostTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "VIS", collectorNumber = "163")
public class GriffinCanyon extends Card {

    public GriffinCanyon() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {T}: Untap target Griffin. If it's a creature, it gets +1/+1 until end of turn.
        var griffin = new PermanentHasSubtypePredicate(CardSubtype.GRIFFIN);
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new UntapPermanentsEffect(TapUntapScope.TARGET, griffin),
                        new ConditionalEffect(new TargetPermanentMatches(new PermanentIsCreaturePredicate()),
                                new BoostTargetPermanentEffect(1, 1, griffin))
                ),
                "{T}: Untap target Griffin. If it's a creature, it gets +1/+1 until end of turn.",
                new PermanentPredicateTargetFilter(griffin, "Target must be a Griffin")
        ));
    }
}
