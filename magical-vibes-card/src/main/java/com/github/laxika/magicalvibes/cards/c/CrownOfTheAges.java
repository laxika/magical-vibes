package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AttachTargetAuraToAnotherPermanentOfSameTypeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAuraAttachedToCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "360")
@CardRegistration(set = "ICE", collectorNumber = "315")
public class CrownOfTheAges extends Card {

    public CrownOfTheAges() {
        // {4}, {T}: Attach target Aura attached to a creature to another creature.
        addActivatedAbility(new ActivatedAbility(
                true,   // requires tap
                "{4}",  // mana cost
                List.of(new AttachTargetAuraToAnotherPermanentOfSameTypeEffect()),
                "{4}, {T}: Attach target Aura attached to a creature to another creature.",
                new PermanentPredicateTargetFilter(new PermanentIsAuraAttachedToCreaturePredicate(),
                        "Target must be an Aura attached to a creature")
        ));
    }
}
