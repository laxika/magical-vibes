package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "CHR", collectorNumber = "82")
@CardRegistration(set = "LEG", collectorNumber = "247")
@CardRegistration(set = "ME3", collectorNumber = "164")
@CardRegistration(set = "SLD", collectorNumber = "1071")
public class PalladiaMors extends Card {

    public PalladiaMors() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new ForcedCostOrElseEffect(
                        new PayManaCost("{R}{G}{W}"),
                        List.of(new SacrificeSelfEffect()),
                        true));
    }
}
