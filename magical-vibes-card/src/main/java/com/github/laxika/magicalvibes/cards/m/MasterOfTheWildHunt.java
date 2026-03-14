package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PackHuntEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M10", collectorNumber = "191")
public class MasterOfTheWildHunt extends Card {

    public MasterOfTheWildHunt() {
        // At the beginning of your upkeep, create a 2/2 green Wolf creature token.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CreateCreatureTokenEffect(
                "Wolf", 2, 2, CardColor.GREEN,
                List.of(CardSubtype.WOLF),
                Set.of(), Set.of()
        ));

        // {T}: Tap all untapped Wolf creatures you control. Each Wolf tapped this way deals damage
        // equal to its power to target creature. That creature deals damage equal to its power
        // divided as its controller chooses among any number of those Wolves.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new PackHuntEffect(CardSubtype.WOLF)),
                "{T}: Tap all untapped Wolf creatures you control. Each Wolf tapped this way deals damage equal to its power to target creature. That creature deals damage equal to its power divided as its controller chooses among any number of those Wolves.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"
                )
        ));
    }
}
