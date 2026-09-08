package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.SacrificePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "JUD", collectorNumber = "94")
public class LavaDart extends Card {

    public LavaDart() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(1));
        addCastingOption(new FlashbackCast(List.of(
                new SacrificePermanentsCost(1, new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN)))));
    }
}
