package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AttachTargetAuraToAnotherPermanentOfSameTypeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAuraAttachedToCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

public class AutumnTailKitsuneSage extends Card {

    public AutumnTailKitsuneSage() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new AttachTargetAuraToAnotherPermanentOfSameTypeEffect(true)),
                "{1}: Attach target Aura attached to a creature to another creature.",
                new PermanentPredicateTargetFilter(new PermanentIsAuraAttachedToCreaturePredicate(),
                        "Target must be an Aura attached to a creature")
        ));
    }
}
