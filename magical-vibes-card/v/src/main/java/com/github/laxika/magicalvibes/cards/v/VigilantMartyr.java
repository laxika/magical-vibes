package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsPermanentPredicate;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "45")
public class VigilantMartyr extends Card {

    public VigilantMartyr() {
        // Sacrifice this creature: Regenerate target creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(), new RegenerateEffect(true)),
                "Sacrifice this creature: Regenerate target creature.",
                TargetFilters.creature()
        ));

        // {W}{W}, {T}, Sacrifice this creature: Counter target spell that targets an enchantment.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}{W}",
                List.of(new SacrificeSelfCost(), new CounterSpellEffect()),
                "{W}{W}, {T}, Sacrifice this creature: Counter target spell that targets an enchantment.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryTargetsPermanentPredicate(new PermanentIsEnchantmentPredicate()),
                        "Target must be a spell that targets an enchantment."
                )
        ));
    }
}
