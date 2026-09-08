package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "82")
@CardRegistration(set = "TPR", collectorNumber = "63")
public class RootwaterHunter extends Card {

    public RootwaterHunter() {
        addActivatedAbility(new ActivatedAbility(true, null, List.of(new DealDamageToAnyTargetEffect(1)), "{T}: Rootwater Hunter deals 1 damage to any target."));
    }
}
