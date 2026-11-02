package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TSB", collectorNumber = "82")
@CardRegistration(set = "LGN", collectorNumber = "130")
@CardRegistration(set = "MKC", collectorNumber = "175")
public class KrosanCloudscraper extends Card {

    public KrosanCloudscraper() {
        addMorph("{7}{G}{G}");
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new ForcedCostOrElseEffect(
                        new PayManaCost("{G}{G}"),
                        List.of(new SacrificeSelfEffect()),
                        true));
    }
}
