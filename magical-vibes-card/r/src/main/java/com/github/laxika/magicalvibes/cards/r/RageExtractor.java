package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageEqualToSpellManaValueToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PhyrexianManaPredicate;

@CardRegistration(set = "NPH", collectorNumber = "91")
public class RageExtractor extends Card {

    public RageExtractor() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new DealDamageEqualToSpellManaValueToAnyTargetEffect(new PhyrexianManaPredicate()));
    }
}
