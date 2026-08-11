package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCreatureFromHandWithManaValueXThenReturnSourceToHandEffect;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "61")
public class MetathranAerostat extends Card {

    public MetathranAerostat() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{X}{U}",
                List.of(new PutCreatureFromHandWithManaValueXThenReturnSourceToHandEffect()),
                "{X}{U}: You may put a creature card with mana value X from your hand onto the battlefield. If you do, return this creature to its owner's hand."
        ));
    }
}
