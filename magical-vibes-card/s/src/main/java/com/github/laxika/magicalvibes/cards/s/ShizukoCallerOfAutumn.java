package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AwardPersistentManaEffect;

@CardRegistration(set = "BOK", collectorNumber = "144")
public class ShizukoCallerOfAutumn extends Card {

    public ShizukoCallerOfAutumn() {
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new AwardPersistentManaEffect(
                        ManaColor.GREEN,
                        new Fixed(3),
                        AwardPersistentManaEffect.Recipient.TARGET_PLAYER));
    }
}
