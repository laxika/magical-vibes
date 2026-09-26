package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "RTR", collectorNumber = "177")
@CardRegistration(set = "GK1", collectorNumber = "67")
@CardRegistration(set = "2X2", collectorNumber = "245")
@CardRegistration(set = "PIO", collectorNumber = "229")
@CardRegistration(set = "C15", collectorNumber = "226")
public class LotlethTroll extends Card {

    public LotlethTroll() {
        // Trample auto-loaded from Scryfall.

        // Discard a creature card: Put a +1/+1 counter on this creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new DiscardCardTypeCost(new CardTypePredicate(CardType.CREATURE), "creature"),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)
                ),
                "Discard a creature card: Put a +1/+1 counter on Lotleth Troll."));

        // {B}: Regenerate this creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(new RegenerateEffect()),
                "{B}: Regenerate Lotleth Troll."));
    }
}
