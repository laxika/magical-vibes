package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionChoiceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "17")
public class PaleWayfarer extends Card {

    public PaleWayfarer() {
        // {2}{W}{W}, {Q}: Target creature gains protection from the color of its controller's choice until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{2}{W}{W}",
                List.of(new GrantProtectionChoiceUntilEndOfTurnEffect(false, true)),
                "{2}{W}{W}, {Q}: Target creature gains protection from the color of its controller's choice until end of turn.",
                new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Target must be a creature"))
                .withRequiresUntap());
    }
}
