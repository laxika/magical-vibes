package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "ONS", collectorNumber = "162")
public class ProfanePrayers extends Card {

    public ProfanePrayers() {
        PermanentCount clericCount = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.CLERIC), CountScope.ANY_PLAYER);
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(clericCount));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(clericCount));
    }
}
