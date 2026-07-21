package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "HOU", collectorNumber = "34")
public class EternalOfHarshTruths extends Card {

    public EternalOfHarshTruths() {
        // Afflict 2 — whenever this creature becomes blocked, the defending player loses 2 life
        // (once per becoming blocked, not per blocker). Heads-up, so the sole opponent is the defender.
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT));
        // Whenever this creature attacks and isn't blocked, draw a card.
        addEffect(EffectSlot.ON_ATTACKS_UNBLOCKED, new DrawCardEffect());
    }
}
