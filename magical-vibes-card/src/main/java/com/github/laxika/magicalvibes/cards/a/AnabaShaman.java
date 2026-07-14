package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

import java.util.List;

@CardRegistration(set = "9ED", collectorNumber = "172")
@CardRegistration(set = "6ED", collectorNumber = "165")
@CardRegistration(set = "8ED", collectorNumber = "175")
public class AnabaShaman extends Card {

    public AnabaShaman() {
        addActivatedAbility(new ActivatedAbility(true, "{R}", List.of(new DealDamageToAnyTargetEffect(1)), "{R}, {T}: Anaba Shaman deals 1 damage to any target."));
    }
}
