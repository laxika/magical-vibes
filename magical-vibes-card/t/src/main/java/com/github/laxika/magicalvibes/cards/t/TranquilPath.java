package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

@CardRegistration(set = "APC", collectorNumber = "89")
public class TranquilPath extends Card {

    public TranquilPath() {
        addSpellEffect(DestroyTargetPermanentEffect.all(new PermanentIsEnchantmentPredicate()));
        addSpellEffect(new DrawCardEffect(1));
    }
}
