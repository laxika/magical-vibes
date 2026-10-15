package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

import java.util.List;

@CardRegistration(set = "WWK", collectorNumber = "79")
public class CunningSparkmage extends Card {

    public CunningSparkmage() {
        // "{T}: This creature deals 1 damage to any target."
        addActivatedAbility(new ActivatedAbility(true, null, List.of(new DealDamageToAnyTargetEffect(1)),
                "{T}: Cunning Sparkmage deals 1 damage to any target."));
    }
}
