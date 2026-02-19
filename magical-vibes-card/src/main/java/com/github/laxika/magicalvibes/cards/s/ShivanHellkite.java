package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

import java.util.List;

@CardRegistration(set = "10E", collectorNumber = "231")
public class ShivanHellkite extends Card {

    public ShivanHellkite() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{R}", List.of(new DealDamageToAnyTargetEffect(1)), true, "{1}{R}: Shivan Hellkite deals 1 damage to any target."));
    }
}
