package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "GS1", collectorNumber = "37")
public class CleansingScreech extends Card {

    public CleansingScreech() {
        addSpellEffect(new DealDamageToAnyTargetEffect(4));
    }
}
