package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyControllerActivatedAbilityTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCardTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntrySubtypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M20", collectorNumber = "131")
public class ChandrasRegulator extends Card {

    public ChandrasRegulator() {
        // Whenever you activate a loyalty ability of a Chandra planeswalker, you may pay {1} to
        // copy that ability. You may choose new targets for the copy.
        addEffect(EffectSlot.ON_CONTROLLER_ACTIVATES_NONMANA_ABILITY,
                new CopyControllerActivatedAbilityTriggerEffect(
                        "{1}",
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryCardTypeInPredicate(Set.of(CardType.PLANESWALKER)),
                                new StackEntrySubtypeInPredicate(Set.of(CardSubtype.CHANDRA)))),
                        false,
                        true));

        // {1}, {T}, Discard a Mountain card or a red card: Draw a card.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new DiscardCardTypeCost(new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.MOUNTAIN),
                                new CardColorPredicate(CardColor.RED))), "Mountain or red"),
                        new DrawCardEffect(1)),
                "{1}, {T}, Discard a Mountain card or a red card: Draw a card."));
    }
}
