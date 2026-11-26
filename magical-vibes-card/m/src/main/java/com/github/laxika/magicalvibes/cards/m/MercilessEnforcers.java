package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "58")
@CardRegistration(set = "OM1", collectorNumber = "61")
public class MercilessEnforcers extends Card {

    public MercilessEnforcers() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{B}",
                List.of(DealDamageEffect.to(Subjects.eachOpponent(), 1)),
                "{3}{B}: This creature deals 1 damage to each opponent."
        ));
    }
}
