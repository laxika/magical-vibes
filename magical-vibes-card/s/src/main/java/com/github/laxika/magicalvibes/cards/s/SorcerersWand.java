package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.condition.SourceHasSubtype;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "231")
public class SorcerersWand extends Card {

    public SorcerersWand() {
        // Equipped creature has "{T}: This creature deals 1 damage to target player or planeswalker.
        // If this creature is a Wizard, it deals 2 damage instead."
        addStaticEffect(new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        null,
                        List.of(new ConditionalReplacementEffect(new SourceHasSubtype(CardSubtype.WIZARD), DealDamageEffect.toAnyTarget(1),
                                DealDamageEffect.toAnyTarget(2)
                        )),
                        "{T}: This creature deals 1 damage to target player or planeswalker. If this creature is a Wizard, it deals 2 damage instead.",
                        new PermanentPredicateTargetFilter(
                                new PermanentIsPlaneswalkerPredicate(),
                                "Target must be a player or planeswalker"
                        )
                ),
                Subjects.attachedCreature()
        ));

        // Equip {3}
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
