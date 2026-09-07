package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "42")
public class AvianOddity extends Card {

    public AvianOddity() {
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(
                        new PutCounterOnTargetPermanentEffect(CounterType.FLYING, 1),
                        new DrawCardEffect(1)),
                "Cycling {2}{U} ({2}{U}, Discard this card: Draw a card.)",
                TargetFilters.creatureYouControl(),
                null,
                null,
                null,
                List.of(),
                0,
                1));
    }
}
