package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.StepKind;

@CardRegistration(set = "RNA", collectorNumber = "187")
@CardRegistration(set = "DMC", collectorNumber = "155")
public class KayasWrath extends Card {

    public KayasWrath() {
        // Destroy all creatures. You gain life equal to the number of creatures you controlled destroyed this way.
        addSpellEffect(new DestroyAllPermanentsEffect(new PermanentIsCreaturePredicate()));
        addSpellEffect(new GainLifeEffect(new AmountThisWay(StepKind.DESTROY).controlledByYou()));
    }
}
