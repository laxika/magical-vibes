package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RedirectTargetCreatureDamageFromChosenSourceToSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "9ED", collectorNumber = "30")
public class OraclesAttendants extends Card {

    public OraclesAttendants() {
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new RedirectTargetCreatureDamageFromChosenSourceToSelfEffect()),
                "{T}: All damage that would be dealt to target creature this turn by a source of your choice is dealt to this creature instead.",
                new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Target must be a creature")));
    }
}
