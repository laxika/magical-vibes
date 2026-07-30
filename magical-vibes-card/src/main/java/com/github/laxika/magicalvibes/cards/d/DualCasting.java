package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AVR", collectorNumber = "133")
public class DualCasting extends Card {

    public DualCasting() {
        // Enchant creature. Enchanted creature has "{R}, {T}: Copy target instant or sorcery spell
        // you control. You may choose new targets for the copy." — the retarget prompt is offered by
        // CopySpellEffectHandler, so the grant only needs the copy effect plus the spell target filter.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                        new ActivatedAbility(
                                true,
                                "{R}",
                                List.of(new CopySpellEffect()),
                                "{R}, {T}: Copy target instant or sorcery spell you control. You may choose new targets for the copy.",
                                new StackEntryPredicateTargetFilter(
                                        new StackEntryAllOfPredicate(List.of(
                                                new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL)),
                                                new StackEntryControlledByPredicate()
                                        )),
                                        "Target must be an instant or sorcery spell you control."
                                )
                        ),
                        GrantScope.ENCHANTED_CREATURE
                ));
    }
}
