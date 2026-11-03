package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ROE", collectorNumber = "153")
@CardRegistration(set = "IMA", collectorNumber = "137")
@CardRegistration(set = "DDJ", collectorNumber = "2")
@CardRegistration(set = "MB1", collectorNumber = "137")
@CardRegistration(set = "HA1", collectorNumber = "10")
public class KilnFiend extends Card {

    public KilnFiend() {
        addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY)
                        ))),
                new BoostSelfEffect(3, 0));
    }
}
