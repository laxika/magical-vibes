package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "161")
public class ConsumingBonfire extends Card {

    public ConsumingBonfire() {
        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Consuming Bonfire deals 4 damage to target non-Elemental creature",
                        DealDamageEffect.toTargetCreature(4),
                        new PermanentPredicateTargetFilter(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentNotPredicate(
                                                new PermanentHasSubtypePredicate(CardSubtype.ELEMENTAL))
                                )),
                                "Target must be a non-Elemental creature."
                        )
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Consuming Bonfire deals 7 damage to target Treefolk creature",
                        DealDamageEffect.toTargetCreature(7),
                        new PermanentPredicateTargetFilter(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentHasSubtypePredicate(CardSubtype.TREEFOLK)
                                )),
                                "Target must be a Treefolk creature."
                        )
                )
        )));
    }
}
