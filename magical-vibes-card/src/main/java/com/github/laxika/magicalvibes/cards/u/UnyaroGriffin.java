package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "6ED", collectorNumber = "49")
public class UnyaroGriffin extends Card {

    public UnyaroGriffin() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(), new CounterSpellEffect()),
                "Sacrifice Unyaro Griffin: Counter target red instant or sorcery spell.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryColorInPredicate(Set.of(CardColor.RED)),
                                new StackEntryTypeInPredicate(Set.of(
                                        StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL))
                        )),
                        "Target must be a red instant or sorcery spell."
                )
        ));
    }
}
