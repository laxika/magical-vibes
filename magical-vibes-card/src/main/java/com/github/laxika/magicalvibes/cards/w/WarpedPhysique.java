package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DGM", collectorNumber = "117")
public class WarpedPhysique extends Card {

    public WarpedPhysique() {
        // Target creature gets +X/-X until end of turn, where X is the number of cards in your hand.
        CardsInHand handSize = new CardsInHand(CountScope.CONTROLLER);
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL,
                new BoostTargetCreatureEffect(handSize, new Scaled(handSize, -1)));
    }
}
