package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "4ED", collectorNumber = "143")
@CardRegistration(set = "ME4", collectorNumber = "88")
@CardRegistration(set = "ARN", collectorNumber = "28")
public class JunNEfreet extends Card {

    public JunNEfreet() {
        // At the beginning of your upkeep, sacrifice this creature unless you pay {B}{B}.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new ForcedCostOrElseEffect(
                        new PayManaCost("{B}{B}"),
                        List.of(new SacrificeSelfEffect()),
                        true));
    }
}
