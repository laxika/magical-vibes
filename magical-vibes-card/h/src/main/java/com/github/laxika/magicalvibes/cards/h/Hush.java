package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

@CardRegistration(set = "USG", collectorNumber = "266")
public class Hush extends Card {

    public Hush() {
        addSpellEffect(DestroyTargetPermanentEffect.all(new PermanentIsEnchantmentPredicate()));
        addCycling("{2}");
    }
}
