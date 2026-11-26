package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "M11", collectorNumber = "133")
public class DestructiveForce extends Card {

    public DestructiveForce() {
        addSpellEffect(new SacrificePermanentsEffect(
                5, new PermanentIsLandPredicate(), Subjects.eachPlayer()));
        addSpellEffect(DealDamageEffect.toEachCreature(5));
    }
}
