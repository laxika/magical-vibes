package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "LRW", collectorNumber = "47")
public class WellgabberApothecary extends Card {

    public WellgabberApothecary() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{W}", List.of(new PreventAllDamageToTargetCreatureEffect()),
                "{1}{W}: Prevent all damage that would be dealt to target tapped Merfolk or Kithkin creature this turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsTappedPredicate(),
                                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.MERFOLK, CardSubtype.KITHKIN))
                        )),
                        "Target must be a tapped Merfolk or Kithkin creature"
                )));
    }
}
