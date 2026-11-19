package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

@CardRegistration(set = "ODY", collectorNumber = "269")
public class Simplify extends Card {

    public Simplify() {
        addSpellEffect(new SacrificePermanentsEffect(
                1, new PermanentIsEnchantmentPredicate(), Subjects.eachPlayer()));
    }
}
