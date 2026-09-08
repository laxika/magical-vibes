package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AwardPersistentManaEffect;

@CardRegistration(set = "DTK", collectorNumber = "231")
public class SavageVentmaw extends Card {

    public SavageVentmaw() {
        addEffect(EffectSlot.ON_ATTACK, new AwardPersistentManaEffect(ManaColor.RED, new Fixed(3)));
        addEffect(EffectSlot.ON_ATTACK, new AwardPersistentManaEffect(ManaColor.GREEN, new Fixed(3)));
    }
}
