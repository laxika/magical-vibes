package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "59")
@CardRegistration(set = "TSR", collectorNumber = "65")
public class DrifterIlDal extends Card {

    public DrifterIlDal() {
        // At the beginning of your upkeep, sacrifice this creature unless you pay {U}.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new ForcedCostOrElseEffect(
                        new PayManaCost("{U}"),
                        List.of(new SacrificeSelfEffect()),
                        true));
    }
}
