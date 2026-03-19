package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerAttachmentEffect;

@CardRegistration(set = "DOM", collectorNumber = "116")
public class ChampionOfTheFlame extends Card {

    public ChampionOfTheFlame() {
        addEffect(EffectSlot.STATIC, new BoostSelfPerAttachmentEffect(2, 2, true, true));
    }
}
