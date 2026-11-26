package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "USG", collectorNumber = "172")
public class AcidicSoil extends Card {

    public AcidicSoil() {
        addSpellEffect(DealDamageEffect.to(Subjects.eachPlayer(),
                new PermanentCount(new PermanentIsLandPredicate(), CountScope.CONTROLLER)));
    }
}
