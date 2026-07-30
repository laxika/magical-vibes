package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.RedirectNextDamageToTargetCreatureToSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HML", collectorNumber = "85")
public class DaughterOfAutumn extends Card {

    public DaughterOfAutumn() {
        // {W}: The next 1 damage that would be dealt to target white creature this turn is dealt to
        // Daughter of Autumn instead.
        addActivatedAbility(new ActivatedAbility(false, "{W}",
                List.of(new RedirectNextDamageToTargetCreatureToSourceEffect(new Fixed(1))),
                "{W}: The next 1 damage that would be dealt to target white creature this turn is dealt to Daughter of Autumn instead.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentColorInPredicate(Set.of(CardColor.WHITE)))),
                        "Target must be a white creature")));
    }
}
