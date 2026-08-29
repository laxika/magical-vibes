package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToDefendingPlayerCreaturesEffect;

@CardRegistration(set = "GRN", collectorNumber = "202")
public class SwathcutterGiant extends Card {

    public SwathcutterGiant() {
        // Whenever this creature attacks, it deals 1 damage to each creature defending player controls.
        addEffect(EffectSlot.ON_ATTACK, new DealDamageToDefendingPlayerCreaturesEffect(1));
    }
}
