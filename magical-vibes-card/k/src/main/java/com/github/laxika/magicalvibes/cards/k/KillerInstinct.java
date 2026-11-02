package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardCreatureToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GPT", collectorNumber = "120")
public class KillerInstinct extends Card {

    public KillerInstinct() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new RevealTopCardCreatureToBattlefieldEffect(true, true));
    }
}
