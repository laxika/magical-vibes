package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.StepKind;

@CardRegistration(set = "ISD", collectorNumber = "26")
public class Paraselene extends Card {

    public Paraselene() {
        // Destroy all enchantments. You gain 1 life for each enchantment destroyed this way.
        addSpellEffect(DestroyTargetPermanentEffect.all(new PermanentIsEnchantmentPredicate()));
        addSpellEffect(new GainLifeEffect(new AmountThisWay(StepKind.DESTROY)));
    }
}
