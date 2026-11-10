package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "158")
public class RottingGiant extends Card {

    public RottingGiant() {
        ForcedCostOrElseEffect sacrificeUnlessExile = new ForcedCostOrElseEffect(
                new ExileCardFromGraveyardCost((CardType) null),
                List.of(new SacrificeSelfEffect()),
                true);
        addTrigger(List.of(Triggers.attacks(), Triggers.blocks()), TriggerOptions.ONE_OR_MORE, sacrificeUnlessExile);
    }
}
