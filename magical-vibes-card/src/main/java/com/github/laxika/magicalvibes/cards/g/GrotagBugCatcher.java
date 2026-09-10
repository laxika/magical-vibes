package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.PartySize;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "ZNR", collectorNumber = "142")
public class GrotagBugCatcher extends Card {

    public GrotagBugCatcher() {
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfEffect(new PartySize(), new Fixed(0)));
    }
}
