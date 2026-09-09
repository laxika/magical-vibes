package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RollD20Effect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "AFR", collectorNumber = "52")
public class ContactOtherPlane extends Card {

    public ContactOtherPlane() {
        addEffect(EffectSlot.SPELL, new RollD20Effect(
                new DrawCardEffect(2),
                SequenceEffect.of(new ScryEffect(2), new DrawCardEffect(2)),
                SequenceEffect.of(new ScryEffect(3), new DrawCardEffect(3))));
    }
}
