package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "160")
public class HeavyweightDemolisher extends Card {

    public HeavyweightDemolisher() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new ForcedCostOrElseEffect(
                        new PayManaCost("{3}"),
                        List.of(new TapPermanentsEffect(TapUntapScope.SELF)),
                        true));

        addUnearth("{6}{R}{R}");
    }
}
