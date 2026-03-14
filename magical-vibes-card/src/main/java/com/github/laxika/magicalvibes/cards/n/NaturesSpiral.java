package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

@CardRegistration(set = "M10", collectorNumber = "196")
@CardRegistration(set = "M11", collectorNumber = "187")
public class NaturesSpiral extends Card {

    public NaturesSpiral() {
        addEffect(EffectSlot.SPELL, new ReturnCardFromGraveyardEffect(
                GraveyardChoiceDestination.HAND,
                new CardIsPermanentPredicate(),
                true
        ));
    }
}
