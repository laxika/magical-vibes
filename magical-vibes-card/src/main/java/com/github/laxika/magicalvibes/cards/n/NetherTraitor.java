package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;

@CardRegistration(set = "TSP", collectorNumber = "120")
public class NetherTraitor extends Card {

    public NetherTraitor() {
        addEffect(EffectSlot.GRAVEYARD_ON_CREATURE_PUT_INTO_CONTROLLER_GRAVEYARD_FROM_BATTLEFIELD,
                new MayPayManaEffect("{B}",
                        new ReturnSourceCardFromGraveyardToBattlefieldEffect(false),
                        "Pay {B} to return Nether Traitor from your graveyard to the battlefield?"));
    }
}
