package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "119")
public class RazorfinHunter extends Card {

    public RazorfinHunter() {
        addActivatedAbility(new ActivatedAbility(true, null, List.of(DealDamageEffect.toAnyTarget(1)), "{T}: Razorfin Hunter deals 1 damage to any target."));
    }
}
