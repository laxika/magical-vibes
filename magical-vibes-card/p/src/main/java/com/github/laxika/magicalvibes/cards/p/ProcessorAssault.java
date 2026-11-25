package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutOpponentOwnedExiledCardIntoGraveyardCost;

@CardRegistration(set = "BFZ", collectorNumber = "132")
public class ProcessorAssault extends Card {

    public ProcessorAssault() {
        addCastingCost(new PutOpponentOwnedExiledCardIntoGraveyardCost());
        addSpellEffect(DealDamageEffect.toTargetCreature(5));
    }
}
