package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "112")
public class ZuranSpellcaster extends Card {

    public ZuranSpellcaster() {
        // "{T}: This creature deals 1 damage to any target."
        addActivatedAbility(new ActivatedAbility(true, null, List.of(new DealDamageToAnyTargetEffect(1)),
                "{T}: Zuran Spellcaster deals 1 damage to any target."));
    }
}
