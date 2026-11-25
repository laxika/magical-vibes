package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextEndStepEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "298")
public class HornOfPlenty extends Card {

    public HornOfPlenty() {
        addTrigger(Triggers.playerCastsSpell().anySpell(),
                UnlessPaysEffect.mayPay(Subjects.triggeringPlayer(), "{1}", RegisterDrawCardsAtNextEndStepEffect.triggeringPlayer()));
    }
}
