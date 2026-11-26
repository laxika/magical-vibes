package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "CHR", collectorNumber = "82")
@CardRegistration(set = "LEG", collectorNumber = "247")
@CardRegistration(set = "ME3", collectorNumber = "164")
@CardRegistration(set = "SLD", collectorNumber = "1071")
public class PalladiaMors extends Card {

    public PalladiaMors() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                UnlessPaysEffect.unlessYouPay("{R}{G}{W}", new SacrificeSelfEffect()));
    }
}
