package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SacrificePermanentsCost;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DivisionMode;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SCG", collectorNumber = "104")
public class SkirkVolcanist extends Card {

    public SkirkVolcanist() {
        addMorph("{0}", new SacrificePermanentsCost(
                2, new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN)));
        target(TargetFilters.creature(), 1, 3).addEffect(EffectSlot.ON_TURNED_FACE_UP,
                new DealDividedDamageEffect(
                        new Fixed(3), null, DivisionMode.CHOSEN,
                        new PermanentIsCreaturePredicate(), 0, false, false, true));
    }
}
