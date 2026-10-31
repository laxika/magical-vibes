package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "DKA", collectorNumber = "88")
public class FiresOfUndeath extends Card {

    public FiresOfUndeath() {
        addSpellEffect(new DealDamageToAnyTargetEffect(2));
        addCastingOption(new FlashbackCast("{5}{B}"));
    }
}
