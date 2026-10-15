package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutOpponentOwnedExiledCardIntoGraveyardCost;

@CardRegistration(set = "BFZ", collectorNumber = "132")
public class ProcessorAssault extends Card {

    public ProcessorAssault() {
        addEffect(EffectSlot.SPELL, new PutOpponentOwnedExiledCardIntoGraveyardCost());
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(5));
    }
}
