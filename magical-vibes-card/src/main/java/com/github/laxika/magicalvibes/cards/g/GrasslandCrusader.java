package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ONS", collectorNumber = "32")
public class GrasslandCrusader extends Card {

    private static final PermanentAllOfPredicate ELF_OR_SOLDIER_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.ELF, CardSubtype.SOLDIER))
    ));

    public GrasslandCrusader() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new BoostTargetCreatureEffect(2, 2, ELF_OR_SOLDIER_CREATURE)),
                "{T}: Target Elf or Soldier creature gets +2/+2 until end of turn.",
                new PermanentPredicateTargetFilter(ELF_OR_SOLDIER_CREATURE, "Target must be an Elf or Soldier creature")
        ));
    }
}
