package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "5DN", collectorNumber = "51")
public class Fleshgrafter extends Card {

    public Fleshgrafter() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new DiscardCardTypeCost(new CardTypePredicate(CardType.ARTIFACT), "an artifact"),
                        new BoostSelfEffect(2, 2)
                ),
                "Discard an artifact card: This creature gets +2/+2 until end of turn."
        ));
    }
}
