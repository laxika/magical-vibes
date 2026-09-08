package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "160")
public class Earthrumbler extends Card {

    public Earthrumbler() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new ExileCardFromGraveyardCost(CardType.ARTIFACT, CardType.CREATURE),
                        AnimatePermanentsEffect.crew()
                ),
                "Exile an artifact or creature card from your graveyard: This Vehicle becomes an artifact creature until end of turn."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(3), AnimatePermanentsEffect.crew()),
                "Crew 3"
        ));
    }
}
