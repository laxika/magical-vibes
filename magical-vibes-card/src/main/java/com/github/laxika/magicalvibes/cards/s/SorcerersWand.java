package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SourceSubtypeReplacementEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "231")
public class SorcerersWand extends Card {

    public SorcerersWand() {
        // Equipped creature has "{T}: This creature deals 1 damage to target player or planeswalker.
        // If this creature is a Wizard, it deals 2 damage instead."
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        null,
                        List.of(new SourceSubtypeReplacementEffect(
                                CardSubtype.WIZARD,
                                new DealDamageToAnyTargetEffect(1),
                                new DealDamageToAnyTargetEffect(2)
                        )),
                        "{T}: This creature deals 1 damage to target player or planeswalker. If this creature is a Wizard, it deals 2 damage instead.",
                        new PermanentPredicateTargetFilter(
                                new PermanentIsPlaneswalkerPredicate(),
                                "Target must be a player or planeswalker"
                        )
                ),
                GrantScope.EQUIPPED_CREATURE
        ));

        // Equip {3}
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
