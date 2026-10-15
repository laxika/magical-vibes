package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "158")
public class RottingGiant extends Card {

    public RottingGiant() {
        ForcedCostOrElseEffect sacrificeUnlessExile = new ForcedCostOrElseEffect(
                new ExileCardFromGraveyardCost((CardType) null),
                List.of(new SacrificeSelfEffect()),
                true);
        addEffect(EffectSlot.ON_ATTACK, sacrificeUnlessExile);
        addEffect(EffectSlot.ON_BLOCK, sacrificeUnlessExile);
    }
}
