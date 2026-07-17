package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AttachTargetAuraToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAuraAttachedToCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "360")
public class CrownOfTheAges extends Card {

    public CrownOfTheAges() {
        // {4}, {T}: Attach target Aura attached to a creature to another creature.
        addActivatedAbility(new ActivatedAbility(
                true,   // requires tap
                "{4}",  // mana cost
                List.of(new AttachTargetAuraToTargetCreatureEffect()),
                "{4}, {T}: Attach target Aura attached to a creature to another creature.",
                List.of(
                        new PermanentPredicateTargetFilter(new PermanentIsAuraAttachedToCreaturePredicate(), "Target must be an Aura attached to a creature"),
                        new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Target must be a creature")
                ),
                2,  // minTargets
                2   // maxTargets
        ));
    }
}
