package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellOrAbilityAndDestroySourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCardTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "5DN", collectorNumber = "90")
public class OupheVandals extends Card {

    public OupheVandals() {
        // "{G}, Sacrifice this creature: Counter target activated ability from an artifact source
        // and destroy that artifact if it's on the battlefield."
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(new SacrificeSelfCost(), new CounterSpellOrAbilityAndDestroySourceEffect()),
                "{G}, Sacrifice Ouphe Vandals: Counter target activated ability from an artifact source and destroy that artifact if it's on the battlefield.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryTypeInPredicate(Set.of(StackEntryType.ACTIVATED_ABILITY)),
                                new StackEntryCardTypeInPredicate(Set.of(CardType.ARTIFACT)))),
                        "Target must be an activated ability from an artifact source.")));
    }
}
