package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KHM", collectorNumber = "45")
public class AvalancheCaller extends Card {

    public AvalancheCaller() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new AnimatePermanentsEffect(
                        4, 4, List.of(CardSubtype.ELEMENTAL), Set.of(Keyword.HEXPROOF, Keyword.HASTE), null, Set.of(),
                        GrantScope.TARGET, EffectDuration.UNTIL_END_OF_TURN)),
                "{2}: Target snow land you control becomes a 4/4 Elemental creature with hexproof and haste until end of turn. It's still a land.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsLandPredicate(),
                                new PermanentHasSupertypePredicate(CardSupertype.SNOW))),
                        "Target must be a snow land you control")
        ));
    }
}
