package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardsForEachOpponentOfTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ODY", collectorNumber = "102")
@CardRegistration(set = "TLE", collectorNumber = "19")
@CardRegistration(set = "MB2", collectorNumber = "172")
public class Standstill extends Card {

    public Standstill() {
        addTrigger(Triggers.playerCastsSpell().anySpell(),
                new SacrificeSelfThenEffect(new DrawCardsForEachOpponentOfTriggeringPlayerEffect(3)));
    }
}
