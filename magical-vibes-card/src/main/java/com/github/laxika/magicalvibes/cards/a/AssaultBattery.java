package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "INV", collectorNumber = "295")
@CardRegistration(set = "TSB", collectorNumber = "106")
public class AssaultBattery extends Card {

    public AssaultBattery() {
        TargetFilter anyTarget = new AnyTargetPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate()
                )),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be any target"
        );

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Assault — Deal 2 damage to any target",
                        new DealDamageToAnyTargetEffect(2),
                        anyTarget
                ).withManaCost("{R}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Battery — Create a 3/3 green Elephant creature token",
                        new CreateTokenEffect("Elephant", 3, 3, CardColor.GREEN,
                                List.of(CardSubtype.ELEPHANT), Set.of(), Set.of())
                ).withManaCost("{3}{G}")
        )));
    }
}
