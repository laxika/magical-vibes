package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "245")
public class TranquilDomain extends Card {

    public TranquilDomain() {
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                new PermanentIsEnchantmentPredicate(),
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.AURA))))));
    }
}
