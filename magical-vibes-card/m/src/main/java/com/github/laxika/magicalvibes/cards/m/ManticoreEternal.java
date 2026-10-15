package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;

@CardRegistration(set = "HOU", collectorNumber = "103")
public class ManticoreEternal extends Card {

    public ManticoreEternal() {
        // Afflict 3 — whenever this creature becomes blocked, defending player loses 3 life
        // (once per becoming blocked, not per blocker).
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new LoseLifeEffect(3, LoseLifeRecipient.DEFENDING_PLAYER));

        // This creature attacks each combat if able.
        addEffect(EffectSlot.STATIC, new MustAttackEffect());
    }
}
