package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RedirectNextDamageToTargetCreatureToSourceEffect;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HML", collectorNumber = "8")
public class HazduhrTheAbbot extends Card {

    public HazduhrTheAbbot() {
        // {X}, {T}: The next X damage that would be dealt this turn to target white creature you
        // control is dealt to Hazduhr the Abbot instead.
        addActivatedAbility(new ActivatedAbility(true, "{X}",
                List.of(new RedirectNextDamageToTargetCreatureToSourceEffect(new XValue())),
                "{X}, {T}: The next X damage that would be dealt this turn to target white creature you control is dealt to Hazduhr the Abbot instead.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentColorInPredicate(Set.of(CardColor.WHITE)))),
                        "Target must be a white creature you control")));
    }
}
