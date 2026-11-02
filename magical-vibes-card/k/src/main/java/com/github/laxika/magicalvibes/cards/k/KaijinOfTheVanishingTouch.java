package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnCombatOpponentToHandAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BOK", collectorNumber = "39")
public class KaijinOfTheVanishingTouch extends Card {

    public KaijinOfTheVanishingTouch() {
        // Whenever this creature blocks a creature, return that creature to its owner's hand at
        // end of combat.
        addTrigger(Triggers.blocks(), new ReturnCombatOpponentToHandAtEndOfCombatEffect());
    }
}
