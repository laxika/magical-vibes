package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnControlledCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "98")
@CardRegistration(set = "ECL", collectorNumber = "311")
public class DawnhandDissident extends Card {

    public DawnhandDissident() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PutCounterOnControlledCreatureCost(CounterType.MINUS_ONE_MINUS_ONE, 1),
                        new SurveilEffect(1)),
                "{T}, Blight 1: Surveil 1."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new PutCounterOnControlledCreatureCost(CounterType.MINUS_ONE_MINUS_ONE, 2),
                        ExileGraveyardCardsEffect.exactTargetedFromAnyGraveyard(1, null, true).imprintingOnSource()),
                "{T}, Blight 2: Exile target card from a graveyard."
        ));

        addStaticEffect(new AllowCastFromCardsExiledWithSourceEffect(
                false,
                new CardTypePredicate(CardType.CREATURE),
                true,
                true,
                3
        ));
    }
}
