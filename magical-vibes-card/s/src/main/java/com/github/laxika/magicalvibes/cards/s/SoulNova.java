package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetAndAttachedMatchingEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MRD", collectorNumber = "25")
public class SoulNova extends Card {

    public SoulNova() {
        target(TargetFilters.attackingCreature())
                .addEffect(EffectSlot.SPELL, new ExileTargetAndAttachedMatchingEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT)));
    }
}
