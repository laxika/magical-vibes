package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AwardPersistentManaEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BOK", collectorNumber = "144")
public class ShizukoCallerOfAutumn extends Card {

    public ShizukoCallerOfAutumn() {
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                new AwardPersistentManaEffect(
                        ManaColor.GREEN,
                        new Fixed(3),
                        AwardPersistentManaEffect.Recipient.TARGET_PLAYER));
    }
}
