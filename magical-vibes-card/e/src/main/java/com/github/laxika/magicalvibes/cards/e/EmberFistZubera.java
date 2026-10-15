package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CreatureSubtypeDeathsThisTurn;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "CHK", collectorNumber = "166")
public class EmberFistZubera extends Card {

    public EmberFistZubera() {
        // When this creature dies, it deals damage to any target equal to the number of Zubera that died this turn.
        addEffect(EffectSlot.ON_DEATH, new DealDamageToAnyTargetEffect(
                new CreatureSubtypeDeathsThisTurn(CardSubtype.ZUBERA, CountScope.ANY_PLAYER)));
    }
}
