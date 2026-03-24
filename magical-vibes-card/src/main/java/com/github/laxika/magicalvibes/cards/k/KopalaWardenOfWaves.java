package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.IncreaseOpponentCostForTargetingControlledSubtypeEffect;

@CardRegistration(set = "XLN", collectorNumber = "61")
public class KopalaWardenOfWaves extends Card {

    public KopalaWardenOfWaves() {
        // Spells your opponents cast that target a Merfolk you control cost {2} more to cast.
        // Abilities your opponents activate that target a Merfolk you control cost {2} more to activate.
        addEffect(EffectSlot.STATIC, new IncreaseOpponentCostForTargetingControlledSubtypeEffect(CardSubtype.MERFOLK, 2));
    }
}
