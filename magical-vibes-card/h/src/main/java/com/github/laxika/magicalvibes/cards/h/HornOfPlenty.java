package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextEndStepEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "298")
public class HornOfPlenty extends Card {

    public HornOfPlenty() {
        addTrigger(Triggers.playerCastsSpell().anySpell(),
                new MayPayManaEffect(
                        "{1}",
                        RegisterDrawCardsAtNextEndStepEffect.triggeringPlayer(),
                        "Pay {1}?",
                        MayPayPayer.TRIGGERING_PLAYER));
    }
}
