package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "104")
public class VoidBeckoner extends Card {

    public VoidBeckoner() {
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(
                        new PutCounterOnTargetPermanentEffect(CounterType.DEATHTOUCH, 1),
                        new DrawCardEffect(1)),
                "Cycling {2}{B} ({2}{B}, Discard this card: Draw a card.)",
                TargetFilters.creatureYouControl(),
                null,
                null,
                null,
                List.of(),
                0,
                1));
    }
}
