package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "15")
public class SamiteCenserBearer extends Card {

    public SamiteCenserBearer() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(new SacrificeSelfCost(), PreventDamageEffect.nextToControlledCreatures(1)),
                "{W}, Sacrifice this creature: Prevent the next 1 damage that would be dealt to each creature you control this turn."
        ));
    }
}
