package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AttachTargetAuraToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAuraAttachedToCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

public class AutumnTailKitsuneSage extends Card {

    public AutumnTailKitsuneSage() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new AttachTargetAuraToTargetCreatureEffect()),
                "{1}: Attach target Aura attached to a creature to another creature.",
                List.of(
                        new PermanentPredicateTargetFilter(
                                new PermanentIsAuraAttachedToCreaturePredicate(),
                                "Target must be an Aura attached to a creature"),
                        TargetFilters.creature()
                ),
                2,
                2
        ));
    }
}
