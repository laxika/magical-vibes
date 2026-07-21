package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "HOU", collectorNumber = "66")
public class KhenraEternal extends Card {

    public KhenraEternal() {
        // Afflict 1 — whenever this creature becomes blocked, the defending player loses 1 life
        // (once per becoming blocked, not per blocker). Heads-up, so the sole opponent is the defender.
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));
    }
}
