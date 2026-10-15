package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "MBS", collectorNumber = "87")
@CardRegistration(set = "TD2", collectorNumber = "62")
public class PlaguemawBeast extends Card {

    public PlaguemawBeast() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeCreatureCost(), new ProliferateEffect()),
                "{T}, Sacrifice a creature: Proliferate."
        ));
    }
}
