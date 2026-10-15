package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.condition.WasCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreatureFromGraveyardToBattlefieldOrHandByManaValueEffect;

@CardRegistration(set = "DMU", collectorNumber = "197")
public class BortukBonerattle extends Card {

    public BortukBonerattle() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new WasCast(),
                new ReturnTargetCreatureFromGraveyardToBattlefieldOrHandByManaValueEffect(
                        new BasicLandTypesAmongControlledLands())));
    }
}
