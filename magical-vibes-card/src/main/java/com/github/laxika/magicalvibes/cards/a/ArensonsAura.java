package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "5ED", collectorNumber = "6")
@CardRegistration(set = "ICE", collectorNumber = "3")
public class ArensonsAura extends Card {

    public ArensonsAura() {
        // {W}, Sacrifice an enchantment: Destroy target enchantment.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(new SacrificePermanentCost(new PermanentIsEnchantmentPredicate(),
                                "Sacrifice an enchantment", false),
                        new DestroyTargetPermanentEffect(false)),
                "{W}, Sacrifice an enchantment: Destroy target enchantment.",
                new PermanentPredicateTargetFilter(new PermanentIsEnchantmentPredicate(),
                        "Target must be an enchantment.")
        ));

        // {3}{U}{U}: Counter target enchantment spell.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}{U}",
                List.of(new CounterSpellEffect()),
                "{3}{U}{U}: Counter target enchantment spell.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryTypeInPredicate(Set.of(StackEntryType.ENCHANTMENT_SPELL)),
                        "Target must be an enchantment spell.")
        ));
    }
}
