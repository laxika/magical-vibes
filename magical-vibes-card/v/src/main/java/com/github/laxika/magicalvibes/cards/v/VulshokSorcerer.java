package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

import java.util.List;

@CardRegistration(set = "5DN", collectorNumber = "80")
@CardRegistration(set = "PD2", collectorNumber = "11")
@CardRegistration(set = "DDI", collectorNumber = "50")
public class VulshokSorcerer extends Card {

    public VulshokSorcerer() {
        addActivatedAbility(new ActivatedAbility(true, null, List.of(new DealDamageToAnyTargetEffect(1)),
                "{T}: Vulshok Sorcerer deals 1 damage to any target."));
    }
}
