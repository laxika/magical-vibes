package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "173")
public class BambooGroveArcher extends Card {

    public BambooGroveArcher() {
        PermanentPredicate creatureWithFlying = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasKeywordPredicate(Keyword.FLYING)
        ));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{4}{G}",
                List.of(new DestroyTargetPermanentEffect(creatureWithFlying)),
                "Channel — {4}{G}, Discard this card: Destroy target creature with flying.",
                new PermanentPredicateTargetFilter(creatureWithFlying, "Target must be a creature with flying")
        ));
    }
}
