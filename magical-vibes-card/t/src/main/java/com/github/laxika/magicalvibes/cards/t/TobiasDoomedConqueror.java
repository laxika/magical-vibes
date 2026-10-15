package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.NontokenCreatureDeathsThisTurn;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DMC", collectorNumber = "45")
@CardRegistration(set = "DMC", collectorNumber = "67")
public class TobiasDoomedConqueror extends Card {

    public TobiasDoomedConqueror() {
        addEffect(EffectSlot.ON_DEATH, new CreateTokenEffect(
                new NontokenCreatureDeathsThisTurn(CountScope.CONTROLLER),
                "Zombie", 2, 2, CardColor.BLACK,
                List.of(CardSubtype.ZOMBIE), Set.of(), Set.of()));
    }
}
