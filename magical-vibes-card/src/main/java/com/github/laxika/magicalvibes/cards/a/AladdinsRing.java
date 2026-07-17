package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

import java.util.List;

@CardRegistration(set = "9ED", collectorNumber = "286")
@CardRegistration(set = "6ED", collectorNumber = "271")
@CardRegistration(set = "8ED", collectorNumber = "291")
@CardRegistration(set = "7ED", collectorNumber = "286")
@CardRegistration(set = "5ED", collectorNumber = "346")
public class AladdinsRing extends Card {

    public AladdinsRing() {
        addActivatedAbility(new ActivatedAbility(true, "{8}", List.of(new DealDamageToAnyTargetEffect(4)), "{8}, {T}: Aladdin's Ring deals 4 damage to any target."));
    }
}
