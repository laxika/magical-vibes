package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeForSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerActivatedAbilityTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCardTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryHasSourceChosenSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CMM", collectorNumber = "709")
@CardRegistration(set = "CMM", collectorNumber = "774")
public class LeoriSparktouchedHunter extends Card {

    public LeoriSparktouchedHunter() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new ChooseSubtypeForSourceEffect(
                        CardSubtype.planeswalkerTypes(), true, "Choose a planeswalker type."));

        addEffect(EffectSlot.ON_CONTROLLER_ACTIVATES_ABILITY,
                new CopyControllerActivatedAbilityTriggerEffect(
                        null,
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryCardTypeInPredicate(Set.of(CardType.PLANESWALKER)),
                                new StackEntryHasSourceChosenSubtypePredicate()))));
    }
}
