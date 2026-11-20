package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "FDN", collectorNumber = "539")
@CardRegistration(set = "MH1", collectorNumber = "130")
public class GoblinOriflamme extends Card {

    public GoblinOriflamme() {
        // Attacking creatures you control get +1/+0.
        addStaticEffect(new StaticBoostEffect(1, 0, Subjects.creaturesYouControl(), new PermanentIsAttackingPredicate()));
    }
}
