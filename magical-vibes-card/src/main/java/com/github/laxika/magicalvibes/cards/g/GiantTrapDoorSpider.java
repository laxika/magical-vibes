package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ExileSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingSourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "293")
@CardRegistration(set = "DKM", collectorNumber = "33")
public class GiantTrapDoorSpider extends Card {

    public GiantTrapDoorSpider() {
        // {1}{R}{G}, {T}: Exile this creature and target creature without flying that's attacking you.
        // The self-exile is part of the resolution, not a cost: if the target is illegal when the
        // ability resolves the whole ability is countered and the Spider stays on the battlefield.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{R}{G}",
                List.of(new ExileTargetPermanentThenEffect(new ExileSelfEffect(), ThenEffectRecipient.CONTROLLER)),
                "{1}{R}{G}, {T}: Exile this creature and target creature without flying that's attacking you.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsAttackingSourceControllerPredicate(),
                                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))
                        )),
                        "Target must be a creature without flying that's attacking you"
                )
        ));
    }
}
