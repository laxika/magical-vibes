package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OTJ", collectorNumber = "220")
public class MarchesaDealerOfDeath extends Card {

    public MarchesaDealerOfDeath() {
        addTrigger(Triggers.youCommitCrime(),
                new MayPayManaEffect("{1}",
                        LookAtTopCardsEffect.chooseExactlyNToHandRestToGraveyard(2, 1),
                        "Pay {1} to look at the top two cards?"));
    }
}
