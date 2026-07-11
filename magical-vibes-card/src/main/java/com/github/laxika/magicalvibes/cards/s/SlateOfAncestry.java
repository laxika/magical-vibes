package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DiscardHandCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "9ED", collectorNumber = "308")
public class SlateOfAncestry extends Card {

    public SlateOfAncestry() {
        // {4}, {T}, Discard your hand: Draw a card for each creature you control.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(
                        new DiscardHandCost(),
                        new DrawCardEffect(new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER))
                ),
                "{4}, {T}, Discard your hand: Draw a card for each creature you control."
        ));
    }
}
