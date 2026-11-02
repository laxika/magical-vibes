package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardAnyOpponentMayGraveyardOrDealDamageEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOI", collectorNumber = "181")
@CardRegistration(set = "SIR", collectorNumber = "177")
public class SinProdder extends Card {

    public SinProdder() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new RevealTopCardAnyOpponentMayGraveyardOrDealDamageEffect());
    }
}
