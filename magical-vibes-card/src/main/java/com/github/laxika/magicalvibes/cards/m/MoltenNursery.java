package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsColorlessPredicate;

import java.util.List;

@CardRegistration(set = "BFZ", collectorNumber = "130")
public class MoltenNursery extends Card {

    public MoltenNursery() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardIsColorlessPredicate(),
                List.of(new DealDamageToAnyTargetEffect(1))
        ));
    }
}
