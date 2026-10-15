package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.GreatestDamageDealtBySourceThisTurn;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;

@CardRegistration(set = "C14", collectorNumber = "36")
@CardRegistration(set = "CM2", collectorNumber = "107")
public class ImpactResonance extends Card {

    public ImpactResonance() {
        addEffect(EffectSlot.SPELL, DealDividedDamageEffect.chosenAmongTargetCreatures(
                new GreatestDamageDealtBySourceThisTurn()));
    }
}
