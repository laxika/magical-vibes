package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DIS", collectorNumber = "141")
public class AzoriusGuildmage extends Card {

    public AzoriusGuildmage() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}",
                List.of(new TapPermanentsEffect(TapUntapScope.TARGET)),
                "{2}{W}: Tap target creature.",
                TargetFilters.creature()
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(new CounterSpellEffect()),
                "{2}{U}: Counter target activated ability. (Mana abilities can't be targeted.)",
                new StackEntryPredicateTargetFilter(
                        new StackEntryTypeInPredicate(Set.of(StackEntryType.ACTIVATED_ABILITY)),
                        "Target must be an activated ability."
                )
        ));
    }
}
