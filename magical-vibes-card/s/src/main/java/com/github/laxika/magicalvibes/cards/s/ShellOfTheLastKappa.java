package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ExileTargetSpellUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastCardExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsYouPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CHK", collectorNumber = "269")
public class ShellOfTheLastKappa extends Card {

    public ShellOfTheLastKappa() {
        // {3}, {T}: Exile target instant or sorcery spell that targets you. The card stays tracked
        // against this artifact so the sacrifice ability below can cast it. Exiling is not
        // countering, so "can't be countered" does not protect the spell.
        addActivatedAbility(new ActivatedAbility(true, "{3}",
                List.of(new ExileTargetSpellUntilSourceLeavesEffect()),
                "{3}, {T}: Exile target instant or sorcery spell that targets you.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryTypeInPredicate(Set.of(
                                        StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL)),
                                new StackEntryTargetsYouPredicate()
                        )),
                        "Target must be an instant or sorcery spell that targets you.")));

        // {3}, {T}, Sacrifice this artifact: You may cast a spell from among cards exiled with this
        // artifact without paying its mana cost. The sacrificed permanent's id survives on the
        // ability's stack entry, so the exiled pile is still reachable on resolution.
        addActivatedAbility(new ActivatedAbility(true, "{3}",
                List.of(new SacrificeSelfCost(), new MayCastCardExiledWithSourceEffect()),
                "{3}, {T}, Sacrifice Shell of the Last Kappa: You may cast a spell from among cards "
                        + "exiled with Shell of the Last Kappa without paying its mana cost."));
    }
}
