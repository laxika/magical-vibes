package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AwardPersistentManaEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BOK", collectorNumber = "142")
public class SakuraTribeSpringcaller extends Card {

    public SakuraTribeSpringcaller() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new AwardPersistentManaEffect(ManaColor.GREEN, new Fixed(1)));
    }
}
