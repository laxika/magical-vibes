package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.PreventCombatDamageFromAttackingCreaturesUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FEM", collectorNumber = "6")
@CardRegistration(set = "FEM", collectorNumber = "143")
public class Heroism extends Card {

    public Heroism() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificePermanentCost(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentColorInPredicate(Set.of(CardColor.WHITE))
                                )),
                                "Sacrifice a white creature",
                                false
                        ),
                        new PreventCombatDamageFromAttackingCreaturesUnlessPaysEffect(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentIsAttackingPredicate(),
                                        new PermanentColorInPredicate(Set.of(CardColor.RED))
                                )),
                                "{2}{R}"
                        )
                ),
                "Sacrifice a white creature: For each attacking red creature, prevent all combat damage that would be dealt by that creature this turn unless its controller pays {2}{R}."
        ));
    }
}
