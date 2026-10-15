package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;

@CardRegistration(set = "OTJ", collectorNumber = "88")
public class ForsakenMiner extends Card {

    public ForsakenMiner() {
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
        addEffect(EffectSlot.GRAVEYARD_ON_CONTROLLER_COMMITS_CRIME,
                new MayPayManaEffect("{B}",
                        new ReturnSourceCardFromGraveyardToBattlefieldEffect(false),
                        "Pay {B} to return Forsaken Miner from your graveyard to the battlefield?"));
    }
}
