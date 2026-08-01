package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SacrificePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "VIS", collectorNumber = "79")
public class Fireblast extends Card {

    public Fireblast() {
        // You may sacrifice two Mountains rather than pay this spell's mana cost.
        addCastingOption(new AlternateHandCast(List.of(
                new SacrificePermanentsCost(2, new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN)))));

        // Fireblast deals 4 damage to any target.
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(4));
    }
}
