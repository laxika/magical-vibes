package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KLD", collectorNumber = "34")
@CardRegistration(set = "KLR", collectorNumber = "36")
public class VisionaryAugmenter extends Card {

    public VisionaryAugmenter() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new PutCountersOnSourceEffect(1, 1, 2),
                "Put two +1/+1 counters on Visionary Augmenter?",
                new CreateTokenEffect(2, "Servo", 1, 1, null,
                        List.of(CardSubtype.SERVO), Set.of(), Set.of(CardType.ARTIFACT))));
    }
}
