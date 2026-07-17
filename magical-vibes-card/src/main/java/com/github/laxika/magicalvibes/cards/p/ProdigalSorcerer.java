package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

import java.util.List;

@CardRegistration(set = "7ED", collectorNumber = "94")
@CardRegistration(set = "6ED", collectorNumber = "88")
@CardRegistration(set = "5ED", collectorNumber = "112")
public class ProdigalSorcerer extends Card {

    public ProdigalSorcerer() {
        addActivatedAbility(new ActivatedAbility(true, null, List.of(new DealDamageToAnyTargetEffect(1)), "{T}: Prodigal Sorcerer deals 1 damage to any target."));
    }
}
