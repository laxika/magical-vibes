package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardToHandCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;

@CardRegistration(set = "WTH", collectorNumber = "130")
public class HarvestWurm extends Card {

    public HarvestWurm() {
        // When this creature enters, sacrifice it unless you return a basic land card from your
        // graveyard to your hand.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ForcedCostOrElseEffect(
                new ReturnCardFromGraveyardToHandCost(CardPredicateUtils.basicLand()),
                List.of(new SacrificeSelfEffect()),
                true));
    }
}
