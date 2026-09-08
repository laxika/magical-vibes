package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.StormEffect;

@CardRegistration(set = "SCG", collectorNumber = "75")
public class TendrilsOfAgony extends Card {

    public TendrilsOfAgony() {
        addEffect(EffectSlot.SPELL, new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(2));
        addEffect(EffectSlot.ON_SELF_CAST, new StormEffect());
    }
}
