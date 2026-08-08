package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnCombatOpponentToHandAtEndOfCombatEffect;

@CardRegistration(set = "BOK", collectorNumber = "39")
public class KaijinOfTheVanishingTouch extends Card {

    public KaijinOfTheVanishingTouch() {
        // Whenever this creature blocks a creature, return that creature to its owner's hand at
        // end of combat.
        addEffect(EffectSlot.ON_BLOCK, new ReturnCombatOpponentToHandAtEndOfCombatEffect());
    }
}
