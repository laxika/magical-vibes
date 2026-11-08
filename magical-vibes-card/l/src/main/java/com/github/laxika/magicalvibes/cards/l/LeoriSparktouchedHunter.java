package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellOrAbilityEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeForSourceEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCardTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryHasSourceChosenSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CMM", collectorNumber = "709")
@CardRegistration(set = "CMM", collectorNumber = "774")
public class LeoriSparktouchedHunter extends Card {

    public LeoriSparktouchedHunter() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new ChooseSubtypeForSourceEffect(
                        CardSubtype.planeswalkerTypes(), true, "Choose a planeswalker type."));

        addTrigger(Triggers.youActivateAbility().withStackEntryFilter(new StackEntryAllOfPredicate(List.of(
                        new StackEntryCardTypeInPredicate(Set.of(CardType.PLANESWALKER)),
                        new StackEntryHasSourceChosenSubtypePredicate()))),
                new CopyTriggeringSpellOrAbilityEffect());
    }
}
